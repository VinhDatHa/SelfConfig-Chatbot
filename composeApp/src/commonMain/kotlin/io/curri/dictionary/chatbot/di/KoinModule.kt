package io.curri.dictionary.chatbot.di

import androidx.lifecycle.SavedStateHandle
import io.curri.dictionary.chatbot.network.HttpClientFactory
import io.curri.dictionary.chatbot.presentation.chat_page.ChatVM
import io.curri.dictionary.chatbot.presentation.settings.SettingViewModel
import io.curri.dictionary.chatbot.providers.GenerationHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
	viewModelOf(::ChatVM)
	viewModelOf(::SettingViewModel)
}

val dataSourceModule = module {
	single { GenerationHandler() }
}

val networkModule = module {
	single<HttpClientEngine> { createHttpClientEngine() }
	single<HttpClient> { HttpClientFactory.create(get()) }
}

expect val platformModule: Module
expect fun createHttpClientEngine(): HttpClientEngine
fun initializeKoin(
	config: (KoinApplication.() -> Unit)? = null
) {
	startKoin {
		config?.invoke(this)
		modules(platformModule, viewModelModule, dataSourceModule, networkModule)
	}
}