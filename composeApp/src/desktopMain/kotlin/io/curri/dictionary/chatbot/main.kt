package io.curri.dictionary.chatbot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.curri.dictionary.chatbot.app.App
import io.curri.dictionary.chatbot.di.initializeKoin

fun main() = application {
	initializeKoin(config = null)
    Window(
        onCloseRequest = ::exitApplication,
        title = "DictionaryChatbot",
    ) {
		App()
    }
}