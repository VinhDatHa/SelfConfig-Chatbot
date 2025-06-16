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


	@Serializable
	@SerialName("together_ai")
	data class TogetherAiProvider(
		override val id: String = "together_ai",
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
	}

	@Serializable
	@SerialName("google")
	data class GoogleProvider(
		override var id: String = "Google",
		override var enabled: Boolean = true,
		override var name: String = "Google",
		override var models: List<ModelFromProvider> = emptyList(),
		var apiKey: String = "",
		var baseUrl: String = "",
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
	}

	companion object {
		val Types by lazy {
			listOf(
				TogetherAiProvider::class,
				GoogleProvider::class
			)
		}
	}
}