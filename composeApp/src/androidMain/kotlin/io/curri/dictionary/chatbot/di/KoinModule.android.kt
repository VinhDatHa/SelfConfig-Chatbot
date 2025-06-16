package io.curri.dictionary.chatbot.di

import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.setupDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
	get() = module {
		single { DataStoreManager(setupDataStore(androidContext())) }
	}

actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()