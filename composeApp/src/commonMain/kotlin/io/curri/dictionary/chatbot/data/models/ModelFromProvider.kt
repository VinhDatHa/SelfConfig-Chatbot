package io.curri.dictionary.chatbot.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelFromProvider(
	@SerialName("id")
	val modelId: String = "",
	@SerialName("display_name")
	val displayName: String = "",
	@SerialName("type")
	val type: ModelType = ModelType.CHAT,
	val inputModalities: List<Modality> = listOf(Modality.TEXT)
)

@Serializable
enum class ModelType {
	CHAT, EMBEDDING, AUDIO, IMAGE;

	companion object {
		fun valueOf(type: String): ModelType {
			return ModelType.entries.firstOrNull {
				it.name == type.uppercase()
			} ?: ModelType.CHAT
		}
	}
}

@Serializable
enum class Modality {
	TEXT, IMAGE
}