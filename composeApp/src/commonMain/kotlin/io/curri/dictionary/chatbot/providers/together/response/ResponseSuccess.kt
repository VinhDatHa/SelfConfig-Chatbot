package io.curri.dictionary.chatbot.providers.together.response

import io.curri.dictionary.chatbot.data.models.MessageRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TogetherAiResponse(
	val id: String,
	@SerialName("object")
	val objectType: String,
	val created: Long,
	val model: String,
	val prompt: List<String>, // or List<Any> if mixed
	val choices: List<Choice>,
	val usage: Usage
)

@Serializable
data class Choice(
	@SerialName("finish_reason")
	val finishReason: String,
	val logprobs: String? = null, // use appropriate type if needed
	val index: Int =0,
	val message: TogetherAiMessage
)

@Serializable
data class TogetherAiMessage(
	@SerialName("role")
	val role: MessageRole,
	val content: String,
	@SerialName("tool_calls")
	val toolCalls: List<String> = emptyList() // or List<Any> if unsure
)

@Serializable
data class Usage(
	@SerialName("prompt_tokens")
	val promptTokens: Int,
	@SerialName("completion_tokens")
	val completionTokens: Int,
	@SerialName("total_tokens")
	val totalTokens: Int,
	@SerialName("cached_tokens")
	val cachedTokens: Int = 0
)