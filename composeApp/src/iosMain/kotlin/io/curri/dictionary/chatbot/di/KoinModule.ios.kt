package io.curri.dictionary.chatbot.di

import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.setupDataStore
import io.curri.dictionary.chatbot.data.database.AppDatabase
import io.curri.dictionary.chatbot.data.getDatabaseBuilder
import io.curri.dictionary.chatbot.data.database.initAppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module


actual val platformModule: Module
	get() = module {
		single { DataStoreManager(setupDataStore()) }
		single<AppDatabase> {
			val builder = getDatabaseBuilder()
			initAppDatabase(builder)
		}
	}

actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create()