package io.curri.dictionary.chatbot.app

import androidx.compose.ui.window.ComposeUIViewController
import io.curri.dictionary.chatbot.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
	configure = {
		initializeKoin()
	}
) {
	App()
}