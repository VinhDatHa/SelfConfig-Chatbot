package io.curri.dictionary.chatbot.data.models.response

import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.ModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ModelResponse(
	val id: String,
	@SerialName("display_name")
	val name: String,
	@SerialName("type")
	val type: String
)

fun ModelResponse.toProvider(): ModelFromProvider {
	return ModelFromProvider(
		modelId = id,
		displayName = name,
		type = ModelType.valueOf(type)
	)
}