package io.curri.dictionary.chatbot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.curri.dictionary.chatbot.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DictionaryChatbot",
    ) {
        App()
    }
}