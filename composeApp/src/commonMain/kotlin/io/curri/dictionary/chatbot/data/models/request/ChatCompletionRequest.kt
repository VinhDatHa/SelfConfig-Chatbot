package io.curri.dictionary.chatbot.data.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
	val model: String,
	val messages: List<ChatMessage>,
	val stream: Boolean = false,
	val temperature: Double? = null,
	@SerialName("top_p")
	val topP: Double? = null,
	val stop: List<String>? = null,
	@SerialName("max_token")
	val maxToken: Int? = null
)

@Serializable
data class ChatMessage(
	val role: String,  // "user", "assistant", "system"
	val content: String
)