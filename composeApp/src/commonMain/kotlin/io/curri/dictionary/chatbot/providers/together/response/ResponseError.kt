package io.curri.dictionary.chatbot.providers.together.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("error")
data class ResponseError(
	val message: String,
	val type: String,
	val code: String
)


@Serializable
data class TogetherAiError(
	val id: String,
	val errorDetail: ResponseError
)