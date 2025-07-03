package io.curri.dictionary.chatbot.data.models.providers

import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ProviderSetting {
	abstract val id: String
	abstract val enabled: Boolean
	abstract val name: String
	abstract val models: List<ModelFromProvider>
	abstract fun addModel(model: ModelFromProvider): ProviderSetting
	abstract fun editModel(model: ModelFromProvider): ProviderSetting
	abstract fun delModel(model: ModelFromProvider): ProviderSetting
	abstract fun copyProvider(
		id: String = this.id,
		enabled: Boolean = this.enabled,
		name: String = this.name,
		models: List<ModelFromProvider> = this.models
	): ProviderSetting

	@Serializable
	@SerialName("together_ai")
	data class TogetherAiProvider(
		override val id: String = "togetherai",
		override val enabled: Boolean = true,
		override val name: String = "Together AI",
		override val models: List<ModelFromProvider>,
		var apiKey: String = "",
		var baseUrl: String = "https://api.together.xyz/v1"
	) : ProviderSetting() {
		override fun addModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models + model)
		}

		override fun editModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models.map {
				if (it.modelId == model.modelId) model.copy() else it
			})
		}

		override fun delModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models.filter { it.modelId != model.modelId })
		}

		override fun copyProvider(id: String, enabled: Boolean, name: String, models: List<ModelFromProvider>): ProviderSetting {
			return this.copy(
				id = id,
				enabled = enabled,
				name = name,
				models = models
			)
		}
	}

//	@Serializable
//	@SerialName("google")
//	data class GoogleProvider(
//		override var id: String = "Google",
//		override var enabled: Boolean = true,
//		override var name: String = "Google",
//		override var models: List<ModelFromProvider> = emptyList(),
//		var apiKey: String = "",
//		var baseUrl: String = "",
//	) : ProviderSetting() {
//		override fun addModel(model: ModelFromProvider): ProviderSetting {
//			return copy(models = models + model)
//		}
//
//		override fun editModel(model: ModelFromProvider): ProviderSetting {
//			return copy(models = models.map { if (it.modelId == model.modelId) model else it })
//		}
//
//		override fun delModel(model: ModelFromProvider): ProviderSetting {
//			return copy(models = models.filter { it.modelId != model.modelId })
//		}
//
//		override fun copyProvider(id: String, enabled: Boolean, name: String, models: List<ModelFromProvider>): ProviderSetting {
//			return this.copy(
//				id = id,
//				enabled = enabled,
//				name = name,
//				models = models
//			)
//		}
//	}

	@Serializable
	@SerialName("openai")
	data class OpenAiProvider(
		override var id: String = "OpenAI",
		override var enabled: Boolean = true,
		override var name: String = "OpenAI",
		override var models: List<ModelFromProvider> = emptyList(),
		var apiKey: String = "sk-",
		var baseUrl: String = "https://api.openai.com/v1",
	) : ProviderSetting() {
		override fun addModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models + model)
		}

		override fun editModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models.map { if (it.modelId == model.modelId) model else it })
		}

		override fun delModel(model: ModelFromProvider): ProviderSetting {
			return copy(models = models.filter { it.modelId != model.modelId })
		}

		override fun copyProvider(id: String, enabled: Boolean, name: String, models: List<ModelFromProvider>): ProviderSetting {
			return this.copy(
				id = id,
				enabled = enabled,
				name = name,
				models = models
			)
		}

	}

	companion object {
		val Types by lazy {
			listOf(
				TogetherAiProvider::class,
				OpenAiProvider::class,
//				GoogleProvider::class
			)
		}
	}
}