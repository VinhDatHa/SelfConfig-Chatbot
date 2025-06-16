package io.curri.dictionary.chatbot.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting

class ShareSheetState {
	private var show by mutableStateOf(false)
	val isShow get() = show

	private var provider by mutableStateOf<ProviderSetting?>(null)
	val currentProvider get() = provider

	fun show(provider: ProviderSetting) {
		this.show = true
		this.provider = provider
	}

	fun dismiss() {
		this.show = false
	}
}

@Composable
fun rememberShareSheetState(): ShareSheetState {
	return ShareSheetState()
}