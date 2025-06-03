package io.curri.dictionary.chatbot.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
	get() = module {
		single<HttpClientEngine> { OkHttp.create() }
	}

actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()