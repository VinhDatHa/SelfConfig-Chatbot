package io.curri.dictionary.chatbot.data.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionResponse(
	val id: String,
	@SerialName("object")
	val responseObject: String,
	val created: Long,
	val choices: List<CompletionChoice>,
	val model: String,
	val usage: TokenUsage? = null
)

@Serializable
data class CompletionChoice(
	val index: Int,
	val text: String,
	val logprobs: String? = null,
	@SerialName("finish_reason")
	val finishReason: String? = null,
	val seed: String? = null,
	val delta: Delta? = null
)

@Serializable
data class Delta(
	@SerialName("token_id")
	val tokenId: Int? = null,
	val role: String? = null,
	val content: String? = null
)

@Serializable
@SerialName("usage")
data class TokenUsage(
	@SerialName("prompt_tokens")
	val promptTokens: Int? = null,
	@SerialName("completion_tokens")
	val completionTokens: Int? = null,
	@SerialName("total_tokens")
	val totalTokens: Int? = null
)