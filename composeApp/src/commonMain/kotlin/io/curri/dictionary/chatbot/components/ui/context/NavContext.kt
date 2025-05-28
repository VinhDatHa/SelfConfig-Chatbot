package io.curri.dictionary.chatbot.components.ui.context

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
	error("No NavController provided")
}