package io.curri.dictionary.chatbot.providers.together

import io.curri.dictionary.chatbot.components.ui.ToastType
import io.curri.dictionary.chatbot.components.ui.toaster
import io.curri.dictionary.chatbot.data.models.MessageChunk
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.ModelType
import io.curri.dictionary.chatbot.data.models.TokenUsage
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessageChoice
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.file_manager.FileManagerUtils
import io.curri.dictionary.chatbot.network.decodeFromJson
import io.curri.dictionary.chatbot.providers.Provider
import io.curri.dictionary.chatbot.providers.TextGenerationParams
import io.curri.dictionary.chatbot.providers.together.response.TogetherAiError
import io.curri.dictionary.chatbot.providers.together.response.TogetherAiMessage
import io.curri.dictionary.chatbot.providers.together.response.TogetherAiResponse
import io.ktor.client.HttpClient
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
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object TogetherAiProvider : Provider<ProviderSetting.TogetherAiProvider>, KoinComponent {

	private val client: HttpClient by inject<HttpClient>(named("clientConfiged"))
	private val fileManager: FileManagerUtils by inject()

	override suspend fun listModels(providerSetting: ProviderSetting.TogetherAiProvider): List<ModelFromProvider> = withContext(Dispatchers.IO) {
		val requestBuilder = HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url("${providerSetting.baseUrl}/models")
			headers {
				bearerAuth(providerSetting.apiKey)
				appendIfNameAbsent(HttpHeaders.ContentType, "application/json")
			}
		}

		return@withContext try {
			val response = client.request(requestBuilder)
			when {
				response.status.isSuccess() -> {
					val rawResult = response.bodyAsText()
					val parsed = rawResult.decodeFromJson<List<ModelFromProvider>>()?.filter { it.type == ModelType.CHAT }
					parsed?.toImmutableList() ?: emptyList()
				}

				response.status.value in 400..499 -> {
					val rawError = response.bodyAsText()
					val parsedError = rawError.decodeFromJson<TogetherAiError>()?.errorDetail?.message
					println("Error: ${response.status}")
					parsedError?.let {
						toaster.show(it, ToastType.ERROR)
					}
					emptyList<ModelFromProvider>()
				}

				else -> {
					println("Error: ${response.status}")
					emptyList<ModelFromProvider>()
				}
			}
		} catch (ex: Exception) {
			ex.printStackTrace()
			emptyList<ModelFromProvider>()
		}
	}


	override suspend fun generateText(
		providerSetting: ProviderSetting.TogetherAiProvider, messages: List<UIMessage>, params: TextGenerationParams
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
			val response = client.request(requestBuilder)
			if (response.status.isSuccess()) {
				try {
					val responseInJson = response.bodyAsText()
					val parsed = responseInJson.decodeFromJson<TogetherAiResponse>()!!
					val firstChoice = parsed.choices.firstOrNull()!!
					val message = firstChoice.message
					val finishReason = firstChoice.finishReason
					val tokenUsage = TokenUsage(
						promptTokens = parsed.usage.promptTokens,
						totalTokens = parsed.usage.totalTokens,
						completionTokens = parsed.usage.completionTokens
					)

					MessageChunk(
						id = parsed.id,
						model = parsed.model,
						choices = listOf(
							UIMessageChoice(
								index = firstChoice.index, delta = null, message = parseMessageJson(message), finishReason = finishReason
							)
						),
						usage = tokenUsage
					)
				} catch (ex: Exception) {
					ex.printStackTrace()
					MessageChunk("", "", emptyList())
				}
			} else {
				try {
					val parsedError = response.bodyAsText().decodeFromJson<TogetherAiError>()
					error("Messaged: ${parsedError?.errorDetail}")
				} catch (ex: Exception) {
					ex.printStackTrace()
				}
				MessageChunk("", "", emptyList())
			}
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
									val imageData = if (part.isLocal) fileManager.getFileInBase64(part.url) else part.url
									add(buildJsonObject {
										put("type", "image_url")
										put("image_url", buildJsonObject {
											put("url", imageData)
										})
									})
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

	private fun parseMessageJson(rawMessage: TogetherAiMessage): UIMessage {
		return UIMessage(role = rawMessage.role, parts = buildList {
			add(UIMessagePart.Text(rawMessage.content))
		})
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