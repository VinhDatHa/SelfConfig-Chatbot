package io.curri.dictionary.chatbot.providers

import io.curri.dictionary.chatbot.data.models.MessageChunk
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface Provider<T : ProviderSetting> {
	suspend fun listModels(providerSetting: T): List<ModelFromProvider>

	suspend fun generateText(
		providerSetting: T,
		messages: List<UIMessage>,
		params: TextGenerationParams,
	): MessageChunk

//	suspend fun streamText(
//		providerSetting: T,
//		messages: List<UIMessage>,
//		params: TextGenerationParams,
//	): Flow<MessageChunk>
}

@Serializable
data class TextGenerationParams(
	val model: ModelFromProvider,
	val temperature: Float? = 0.6f,
	val topP: Float = 1f
)