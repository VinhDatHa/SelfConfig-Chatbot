package io.curri.dictionary.chatbot

import android.app.Application
import io.curri.dictionary.chatbot.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		initializeKoin {
			androidContext(this@MainApplication)
		}
	}
}