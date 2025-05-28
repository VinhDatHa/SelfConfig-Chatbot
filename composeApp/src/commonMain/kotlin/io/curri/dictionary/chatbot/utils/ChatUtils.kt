package io.curri.dictionary.chatbot.utils

import androidx.navigation.NavController
import io.curri.dictionary.chatbot.Screen
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun NavController.newChat(
	chatId: String
) {
	navigate(Screen.ChatPage(chatId)) {
		popUpTo(0) {
			inclusive = true
		}
		launchSingleTop = true
	}
}