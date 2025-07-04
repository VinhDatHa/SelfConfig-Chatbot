package io.curri.dictionary.chatbot.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
	@SerialName("system") SYSTEM,
	@SerialName("user") USER,
	@SerialName("assistant") ASSISTANT,
	@SerialName("tool") TOOL
}