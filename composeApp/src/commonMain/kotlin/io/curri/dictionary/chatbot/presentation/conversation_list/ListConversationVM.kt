package io.curri.dictionary.chatbot.presentation.conversation_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.Settings
import io.curri.dictionary.chatbot.data.data_store.findModelById
import io.curri.dictionary.chatbot.data.data_store.findProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.repository.ConversationRepository
import io.curri.dictionary.chatbot.file_manager.FileManagerUtils
import io.curri.dictionary.chatbot.presentation.common_state.ScreenState
import io.curri.dictionary.chatbot.providers.ProviderManager
import io.curri.dictionary.chatbot.providers.TextGenerationParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListConversationVM(
	dataStore: DataStoreManager,
	private val conversationRepo: ConversationRepository,
	private val fileManagerUtils: FileManagerUtils
) : ViewModel() {

	private val settings: StateFlow<Settings> = dataStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Lazily, Settings())
	private val currentModelChat = dataStore.settingsFlow.map {
		it.providers.findModelById(it.chatModelId)
	}.stateIn(viewModelScope, SharingStarted.Lazily, null)

	private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
	val conversation: StateFlow<List<Conversation>>
		get() = _conversations.asStateFlow()

	private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)

	private val _errorFlow = MutableSharedFlow<Throwable>()
	val errorFlow = _errorFlow.asSharedFlow()

	val screenState: StateFlow<ScreenState>
		get() = _screenState.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000L),
			ScreenState.Idle
		)

	fun init() {
		viewModelScope.launch(Dispatchers.IO) {
			conversationRepo.getAllConversation().onStart {
				_screenState.update { ScreenState.Loading }
			}
				.collectLatest { conversations ->
					_screenState.update { ScreenState.Idle }
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

	fun generateTitle(conversation: Conversation) {
		if (currentModelChat.value == null) return

		val savedModelChatId = settings.value.chatModelId
		val providerList = settings.value.providers

		if (savedModelChatId.isBlank()) error("No model found for chatModelId=$savedModelChatId")

		val savedModel = providerList.findModelById(savedModelChatId)
			?: error("No model found for chatModelId=$savedModelChatId")

		viewModelScope.launch {
			try {
				val modelSetting = savedModel.findProvider(providerList) ?: run {
					_errorFlow.emit(IllegalStateException("Provider model not resolved"))
					return@launch
				}

				val providerHandler = ProviderManager.getProviderByType(modelSetting)

				val result = providerHandler.generateText(
					providerSetting = modelSetting,
					messages = listOf(
						UIMessage.user(
							"""
                    You are an assistant skilled in conversation. 
                    I will give you some dialogue content within content, and you need to summarize the user's conversation into a title within 15 characters.
                    1. The title's language must match the user's primary language
                    2. Do not use punctuation marks or other special symbols
                    3. Reply with the title only
                    4. Summarize in the language en
                    <content>
                    ${conversation.messages.joinToString("\n\n") { it.summaryAsText() }}
                    </content>
                    """.trimIndent()
						)
					),
					params = TextGenerationParams(
						model = savedModel,
						temperature = 0.3f
					)
				)

				val titleChoice = result.choices.firstOrNull()
				val title = titleChoice?.message?.toText()?.trim()

				if (!title.isNullOrBlank()) {
					conversationRepo.updateConversation(
						conversation = conversation.copy(title = title)
					)
				} else {
					_errorFlow.emit(IllegalStateException("Empty title from response"))
				}
			} catch (ex: Exception) {
				_errorFlow.emit(ex)
				ex.printStackTrace()
			}
		}
	}
}