package io.curri.dictionary.chatbot.data.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Assistant(
	val id: String = Uuid.random().toString(),
	val name: String = "",
	val systemPrompt: String = "",
	val temperature: Float = 0.6f,
	val enableMemory: Boolean = false,
)

@Serializable
data class AssistantMemory(
	val id: Int,
	val content: String = "",
)