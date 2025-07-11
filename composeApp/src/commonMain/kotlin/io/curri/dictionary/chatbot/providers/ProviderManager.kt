package io.curri.dictionary.chatbot.providers

import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.providers.openai.OpenAIProvider
import io.curri.dictionary.chatbot.providers.together.TogetherAiProvider
import kotlin.reflect.KClass

object ProviderManager {
	private val providers = mutableMapOf<String, Provider<*>>()

	init {
		// 注册默认Provider
		registerProvider("togetherai", TogetherAiProvider)
		registerProvider("openai", OpenAIProvider)
//		registerProvider("google", ProviderSetting.GoogleProvider)
	}

	/**
	 * 注册Provider实例
	 *
	 * @param name Provider名称
	 * @param provider Provider实例
	 */
	fun registerProvider(name: String, provider: Provider<*>) {
		providers[name] = provider
	}

	/**
	 * 获取Provider实例
	 *
	 * @param name Provider名称
	 * @return Provider实例，如果不存在则返回null
	 */
	fun getProvider(name: String): Provider<*> {
		return providers[name] ?: throw IllegalArgumentException("Provider not found: $name")
	}

	/**
	 * 根据ProviderSetting获取对应的Provider实例
	 *
	 * @param setting Provider设置
	 * @return Provider实例，如果不存在则返回null
	 */
	fun <T : ProviderSetting> getProviderByType(setting: T): Provider<T> {
		@Suppress("UNCHECKED_CAST")
		return when (setting) {
			is ProviderSetting.TogetherAiProvider -> getProvider("togetherai")
//			is ProviderSetting.GoogleProvider -> getProvider("google")
			is ProviderSetting.OpenAiProvider -> getProvider("openai")
			else -> throw IllegalArgumentException("Provider not found")
		} as Provider<T>
	}

	fun findProviderType(selected: KClass<*>): KClass<out ProviderSetting>? {
		return ProviderSetting.Types.find { it == selected }
	}
}