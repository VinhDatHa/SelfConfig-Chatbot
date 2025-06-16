package io.curri.dictionary.chatbot.data.data_store

import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Settings(
	val chatModelId: String = Uuid.random().toString(),
	val titleModelId: String = Uuid.random().toString(),
	val providers: List<ProviderSetting> = DEFAULT_PROVIDERS,
	val listModels: List<ModelFromProvider> = emptyList(),
	val dynamicColor: Boolean = true,
)

fun List<ProviderSetting>.findModelById(uuid: String): ModelFromProvider? {
	this.forEach { setting ->
		setting.models.forEach { model ->
			if (model.modelId == uuid) {
				return model
			}
		}
	}
	return null
}

fun ModelFromProvider.findProvider(providers: List<ProviderSetting>): ProviderSetting? {
	providers.forEach { setting ->
		setting.models.forEach { model ->
			if (model.modelId == this.modelId) {
				return setting
			}
		}
	}
	return null
}

internal val DEFAULT_PROVIDERS = listOf(
	ProviderSetting.TogetherAiProvider(
		id = Uuid.parse("f4e66e5d-6cb3-4e8e-af3a-cad2b943f296").toString(),
		name = "TogetherAI",
		baseUrl = "https://api.together.xyz/v1",
		apiKey = "",
		enabled = true,
		models = emptyList()
	),
	ProviderSetting.GoogleProvider(
		id = Uuid.parse("1eeea727-9ee5-4cae-93e6-6fb01a4d051e").toString(),
		name = "OpenRouter",
		baseUrl = "https://openrouter.ai/api/v1",
		apiKey = "",
		enabled = false
	)
)