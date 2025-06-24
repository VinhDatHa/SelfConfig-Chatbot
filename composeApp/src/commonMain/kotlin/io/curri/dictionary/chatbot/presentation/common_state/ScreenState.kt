package io.curri.dictionary.chatbot.presentation.common_state

sealed class ScreenState {
	data object Idle : ScreenState()
	data object Error: ScreenState()
	data object Loading: ScreenState()
}