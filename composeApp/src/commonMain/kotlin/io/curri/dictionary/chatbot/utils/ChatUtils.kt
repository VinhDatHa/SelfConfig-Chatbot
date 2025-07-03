package io.curri.dictionary.chatbot.utils

import androidx.navigation.NavController
import io.curri.dictionary.chatbot.app.Screen

fun NavController.newChat(
	chatId: String
) {
	navigate(Screen.ChatPage(chatId)) {
		launchSingleTop = true
	}
}