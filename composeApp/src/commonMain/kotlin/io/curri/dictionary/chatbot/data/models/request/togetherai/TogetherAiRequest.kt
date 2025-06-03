package io.curri.dictionary.chatbot.data.models.request.togetherai

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TogetherRequest(
	@SerialName("model")
	val modelId: String,
	@SerialName("messages")
	val messages: List<TogetherMessageRequest>,
	@SerialName("stream")
	val isStreaming: Boolean = false
)

@Serializable
data class TogetherMessageRequest(
	@SerialName("role")
	val role: String,
	@SerialName("content")
	val content: ImmutableList<TogetherContentPart>
)

@Serializable
sealed class TogetherContentPart {
	abstract val type: String

	@Serializable
	data class Text(
		override val type: String = "text",
		val text: String
	) : TogetherContentPart()

	@Serializable
	data class ImageUrl(
		override val type: String = "image_url",
		val url: TogetherUrl
	) : TogetherContentPart()
}

@Serializable
data class TogetherUrl(
	val url: String
)