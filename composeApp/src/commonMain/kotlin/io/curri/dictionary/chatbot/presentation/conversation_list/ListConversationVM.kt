package io.curri.dictionary.chatbot.presentation.conversation_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.repository.ConversationRepository
import io.curri.dictionary.chatbot.file_manager.FileManagerUtils
import io.curri.dictionary.chatbot.presentation.common_state.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListConversationVM(
	private val conversationRepo: ConversationRepository,
	private val fileManagerUtils: FileManagerUtils
) : ViewModel() {

	private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
	val conversation: StateFlow<List<Conversation>>
		get() = _conversations.asStateFlow()

	private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
	val screenState: StateFlow<ScreenState>
		get() = _screenState.asStateFlow()

	fun init() {
		viewModelScope.launch(Dispatchers.IO) {
			conversationRepo.getAllConversation().collectLatest { conversations ->
				_conversations.value = conversations
			}
		}
	}

	fun delete(conversation: Conversation) {
		viewModelScope.launch {
			conversationRepo.deleteConversation(conversation)
			fileManagerUtils.deleteFile(conversation.files)
		}
	}
}