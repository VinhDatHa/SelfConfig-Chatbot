package io.curri.dictionary.chatbot.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class UIMessage(
	val id: String = Uuid.random().toString(),
	val role: MessageRole,
	val parts: List<UIMessagePart>,
) {
	private fun appendChunk(chunk: MessageChunk): UIMessage {
		val choice = chunk.choices[0]
		return choice.delta?.let { delta ->
			val newParts = delta.parts.fold(parts) { acc, deltaPart ->
				when (deltaPart) {
					is UIMessagePart.Text -> {
						val existingTextPart =
							acc.find { it is UIMessagePart.Text } as? UIMessagePart.Text
						if (existingTextPart != null) {
							acc.map { part ->
								if (part is UIMessagePart.Text) {
									UIMessagePart.Text(existingTextPart.text + deltaPart.text)
								} else part
							}
						} else {
							acc + UIMessagePart.Text(deltaPart.text)
						}
					}

					else -> {
						println("delta part append not supported: $deltaPart")
						acc
					}
				}
			}
			copy(
				parts = newParts
			)
		} ?: this
	}

	fun isValidToUpload() = parts.any {
//		it !is UIMessagePart.Search && it !is UIMessagePart.Reasoning
		it is UIMessagePart.Text && it !is UIMessagePart.Image
	}

	fun isValidToShowActions() = parts.any {
		(it is UIMessagePart.Text && it.text.isNotEmpty()) || it is UIMessagePart.Image
	}

	inline fun <reified P : UIMessagePart> hasPart(): Boolean {
		return parts.any {
			it is P
		}
	}

	fun toText() = parts.joinToString(separator = "\n") { part ->
		when (part) {
			is UIMessagePart.Text -> part.text
			else -> ""
		}
	}

	operator fun plus(chunk: MessageChunk): UIMessage {
		return this.appendChunk(chunk)
	}

	companion object {
		fun system(prompt: String) = UIMessage(
			role = MessageRole.SYSTEM,
			parts = listOf(UIMessagePart.Text(prompt))
		)

		fun user(prompt: String) = UIMessage(
			role = MessageRole.USER,
			parts = listOf(UIMessagePart.Text(prompt))
		)
	}
}

@Serializable
sealed class UIMessagePart {
	@Serializable
	data class Text(val text: String) : UIMessagePart()

	@Serializable
	data class Image(val url: String) : UIMessagePart()

	@Serializable
	data class ToolResult(
		val toolCallId: String,
		val toolName: String,
		val content: JsonElement,
		val arguments: JsonElement
	): UIMessagePart()
}

@Serializable
data class MessageChunk(
	val id: String,
	val model: String,
	val choices: List<UIMessageChoice>
)

@Serializable
data class UIMessageChoice(
	val index: Int,
	val delta: UIMessage?,
	val message: UIMessage?,
	val finishReason: String?
)

// Extension
fun List<UIMessage>.handleMessageChunk(chunk: MessageChunk): List<UIMessage> {
	require(this.isNotEmpty()) {
		"messages must not be empty"
	}
	val choice = chunk.choices[0]
	val message = choice.delta ?: choice.message ?: throw Exception("delta/message is null")
	if (this.last().role != message.role) {
		return this + message
	} else {
		val last = this.last() + chunk
		return this.dropLast(1) + last
	}
}

fun List<UIMessagePart>.isEmptyMessage(): Boolean {
	if (this.isEmpty()) return true
	return this.all { message ->
		when (message) {
			is UIMessagePart.Text -> message.text.isBlank()
			is UIMessagePart.Image -> message.url.isBlank()
			else -> false
		}
	}
}