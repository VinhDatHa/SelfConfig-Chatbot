package io.curri.dictionary.chatbot.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ModelFromProvider(
	val modelId: String = "",
	val displayName: String = "",
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