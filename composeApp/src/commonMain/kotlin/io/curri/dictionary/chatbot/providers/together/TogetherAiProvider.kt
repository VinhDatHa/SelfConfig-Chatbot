package io.curri.dictionary.chatbot.providers.together

import io.curri.dictionary.chatbot.data.models.MessageChunk
import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.ModelType
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessageChoice
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.network.jsonConfig
import io.curri.dictionary.chatbot.providers.Provider
import io.curri.dictionary.chatbot.providers.TextGenerationParams
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.appendIfNameAbsent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object TogetherAiProvider : Provider<ProviderSetting.TogetherAiProvider>, KoinComponent {

	private val client: HttpClient by inject()

	override suspend fun listModels(providerSetting: ProviderSetting.TogetherAiProvider): List<ModelFromProvider> = withContext(Dispatchers.IO) {
		val requestBuilder = HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url("${providerSetting.baseUrl}/models")
			println("Url ${providerSetting.baseUrl}/models")
			headers {
				bearerAuth(providerSetting.apiKey)
				appendIfNameAbsent(HttpHeaders.ContentType, "application/json")
			}
		}

		runCatching {
			val response = client.request(requestBuilder)
			val result = if (response.status.isSuccess()) {
				val rawResponse = response.body<List<ModelFromProvider>>()
				val modelResult = rawResponse.filter { it.type == ModelType.CHAT }
				modelResult.toImmutableList()
			} else {
				emptyList()
			}
			result
		}.onFailure {
			it.printStackTrace()
		}.getOrNull() ?: emptyList()
	}


	override suspend fun generateText(
		providerSetting: ProviderSetting.TogetherAiProvider,
		messages: List<UIMessage>,
		params: TextGenerationParams
	): MessageChunk {
		return withContext(Dispatchers.IO) {
			val requestBody = buildChatCompletionRequest(
				messages, params, false
			)
			val requestBuilder = HttpRequestBuilder().apply {
				method = HttpMethod.Post
				url("${providerSetting.baseUrl}/chat/completions")
				headers {
					appendIfNameAbsent(HttpHeaders.ContentType, "application/json")
					bearerAuth(providerSetting.apiKey)
				}
				println("Request body: $requestBody")
				setBody(requestBody)
			}
			return@withContext runCatching {
				val response = client.request(requestBuilder)
				val result = response.bodyAsText()
				val bodyJson = jsonConfig.parseToJsonElement(result).jsonObject
				val id = bodyJson["id"]?.jsonPrimitive?.contentOrNull ?: ""
				val model = bodyJson["model"]?.jsonPrimitive?.contentOrNull ?: ""
				val choice = bodyJson["choices"]?.jsonArray?.get(0)?.jsonObject

				if (choice != null) {
					val message = choice["message"]?.jsonObject ?: throw Exception("message is null")
					val finishReason = choice["finish_reason"]?.jsonPrimitive?.content ?: "unknown"
					MessageChunk(
						id = id, model = model, choices = listOf(
							UIMessageChoice(
								index = 0, delta = null,
								message = parseMessage(message),
								finishReason = finishReason
							)
						)
					)
				} else {
					bodyJson["error"]?.jsonObject?.let { errorObject ->
						val messageError = errorObject["message"]?.jsonPrimitive?.contentOrNull
						MessageChunk(
							id = id, model = model, choices = listOf(
								UIMessageChoice(
									index = 0,
									message = UIMessage(
										role = MessageRole.SYSTEM,
										parts = buildList {
											UIMessagePart.Text(messageError ?: "Unknown error. Please try again later")
										}
									),
									finishReason = null,
									delta = null
								)
							)
						)
					}

				}

			}.onFailure {
				println("Message error: $it")
				throw Exception(it.message)
			}.getOrNull() ?: MessageChunk("", "", emptyList())
		}
	}

	private fun List<UIMessage>.toMessageJson() = buildJsonArray {
		this@toMessageJson.filter { it.isValidToUpload() }.forEachIndexed { index, message ->
			/* ToDo Handle role
				 if (message.role == MessageRole.TOOL) {
                    message.getToolResults().forEach { result ->
                        add(buildJsonObject {
                            put("role", "tool")
                            put("name", result.toolName)
                            put("tool_call_id", result.toolCallId)
                            put("content", json.encodeToString(result.content))
                        })
                    }
                    return@forEachIndexed
                }
				 */
			add(buildJsonObject {
				put("role", message.role.name.lowercase())

				if (message.parts.size == 1 && message.parts[0] is UIMessagePart.Text) {
					put("content", (message.parts[0] as UIMessagePart.Text).text)
				} else {
					putJsonArray("content") {
						message.parts.forEach { part ->
							when (part) {
								is UIMessagePart.Text -> {
									add(buildJsonObject {
										put("type", "text")
										put("text", part.text)
									})
								}

								is UIMessagePart.Image -> {
									add(buildJsonObject {
										put("type", "image_url")
										put("image_url", buildJsonObject {
											put("url", part.url)
										})
									})
									/* ToDo build content for Image
										add(buildJsonObject {
											part.encodeBase64().onSuccess {
												put("type", "image_url")
												put("image_url", buildJsonObject {
													put("url", it)
												})
											}.onFailure {
												it.printStackTrace()
												println("encode image failed: ${part.url}")

												put("type", "text")
												put("text", "")
											}
										})
										 */
								}

								else -> {

								}
							}
						}
					}
				}
			})
		}
	}

	private fun parseMessage(jsonObject: JsonObject): UIMessage {
		val role = MessageRole.valueOf(
			jsonObject["role"]?.jsonPrimitive?.contentOrNull?.uppercase() ?: "ASSISTANT"
		)

		// 也许支持其他模态的输出content? 暂时只支持文本吧
		val content = jsonObject["content"]?.jsonPrimitive?.contentOrNull ?: ""
//		val reasoning = jsonObject["reasoning_content"] ?: jsonObject["reasoning"]
//		val toolCalls = jsonObject["tool_calls"] as? JsonArray ?: JsonArray(emptyList())

		return UIMessage(
			role = role,
			parts = buildList {
				/*
				if (reasoning?.jsonPrimitive?.contentOrNull != null) {
					add(
						UIMessagePart.Reasoning(
							reasoning = reasoning.jsonPrimitive.contentOrNull ?: ""
						)
					)
				}
				toolCalls.forEach { toolCalls ->
					val type = toolCalls.jsonObject["type"]?.jsonPrimitive?.contentOrNull
					if (type != null && type != "function") error("tool call type not supported: $type")
					val toolCallId = toolCalls.jsonObject["id"]?.jsonPrimitive?.contentOrNull
					val toolName =
						toolCalls.jsonObject["function"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull
					val arguments =
						toolCalls.jsonObject["function"]?.jsonObject?.get("arguments")?.jsonPrimitive?.contentOrNull
					add(
						UIMessagePart.ToolCall(
							toolCallId = toolCallId ?: "",
							toolName = toolName ?: "",
							arguments = arguments ?: ""
						)
					)
				}
				 */

				add(UIMessagePart.Text(content))
			},
			/* ToDo Annotations
			annotations = parseAnnotations(
				jsonObject["annotations"]?.jsonArray ?: JsonArray(
					emptyList()
				)
			),
			 */

		)
	}

	private fun buildChatCompletionRequest(
		messages: List<UIMessage>,
		params: TextGenerationParams,
		stream: Boolean = false,
	): JsonObject {
		return buildJsonObject {
			put("model", params.model.modelId)
			put("messages", messages.toMessageJson())
			put("temperature", params.temperature)
			put("top_p", params.topP)
			put("stream", stream)
			/* ToDo Tools and reasoning
						if (params.model.abilities.contains(ModelAbility.TOOL) && params.tools.isNotEmpty()) {
				putJsonArray("tools") {
					params.tools.forEach { tool ->
						add(buildJsonObject {
							put("type", "function")
							put("function", buildJsonObject {
								put("name", tool.name)
								put("description", tool.description)
								put(
									"parameters",
									json.encodeToJsonElement(
										Schema.serializer(),
										tool.parameters
									)
								)
							})
						})
					}
				}
			}
			 */

		}
	}
}