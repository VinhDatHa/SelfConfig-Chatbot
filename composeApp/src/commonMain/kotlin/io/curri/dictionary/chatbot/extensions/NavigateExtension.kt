package io.curri.dictionary.chatbot.extensions

import androidx.navigation.NavController
import io.curri.dictionary.chatbot.app.Screen

fun NavController.openChatPage(id: String) {
	navigate(Screen.ChatPage(id)) {
		launchSingleTop = true
		popUpTo<Screen.HomeScreen> {
		}
	}
}