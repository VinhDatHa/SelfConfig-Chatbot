package io.curri.dictionary.chatbot.presentation.search_page

import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchVM(
	private val conversationRepository: ConversationRepository
) : ViewModel() {

	private val _searchValue = MutableStateFlow<String>("")
	val searchValue = _searchValue.asStateFlow()
	private val _allConversation = conversationRepository.getAllConversation()


	val searchResult = _searchValue.debounce(600)
		.distinctUntilChanged()
		.combine(_allConversation) { query, conversations ->
			if (query.isBlank()) {
				conversations
			} else {
				conversations.filter {
					it.title.lowercase().contains(query)
				}
			}
		}.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000L),
			emptyList()
		)

	fun performSearch(input: String) {
		viewModelScope.launch(Dispatchers.IO) {
			_searchValue.update { input }
		}
	}
}