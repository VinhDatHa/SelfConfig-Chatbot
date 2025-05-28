package io.curri.dictionary.chatbot

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform