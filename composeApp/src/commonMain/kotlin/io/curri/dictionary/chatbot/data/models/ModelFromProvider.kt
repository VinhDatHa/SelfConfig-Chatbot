package io.curri.dictionary.chatbot.data.models

import io.curri.dictionary.chatbot.data.models.serializer.ModelTypeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelFromProvider(
	@SerialName("id")
	val modelId: String = "",
	@SerialName("display_name")
	val displayName: String = "",
	@SerialName("type")
	@Serializable(with = ModelTypeSerializer::class)
	val type: ModelType = ModelType.CHAT,

	val inputModalities: List<Modality> = listOf(Modality.TEXT)
)

@Serializable
enum class ModelType {
	CHAT, EMBEDDING, AUDIO, IMAGE, UNKNOWN;

	companion object {
		fun valueOf(type: String): ModelType {
			return ModelType.entries.firstOrNull {
				it.name == type.uppercase()
			} ?: CHAT
		}
	}
}

@Serializable
enum class Modality {
	TEXT, IMAGE
}