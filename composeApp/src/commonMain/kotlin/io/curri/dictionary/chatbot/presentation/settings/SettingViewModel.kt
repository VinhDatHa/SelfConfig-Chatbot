package io.curri.dictionary.chatbot.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(
	private val dataStore: DataStoreManager
) : ViewModel() {
	val settings: StateFlow<Settings> = dataStore.settingsFlow
		.stateIn(viewModelScope, SharingStarted.Lazily, Settings())

	fun updateSettings(settings: Settings) {
		viewModelScope.launch {
			dataStore.update(settings)
		}
	}
}