package io.curri.dictionary.chatbot.presentation.chat_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.Settings
import io.curri.dictionary.chatbot.data.data_store.findModelById
import io.curri.dictionary.chatbot.data.data_store.findProvider
import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import io.curri.dictionary.chatbot.data.models.isEmptyMessage
import io.curri.dictionary.chatbot.data.repository.ConversationRepository
import io.curri.dictionary.chatbot.providers.GenerationHandler
import io.curri.dictionary.chatbot.providers.ProviderManager
import io.curri.dictionary.chatbot.providers.TextGenerationParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ChatVM(
	savedStateHandle: SavedStateHandle,
	private val dataStore: DataStoreManager,
	private val generationHandler: GenerationHandler,
	private val conversationRepository: ConversationRepository
) : ViewModel() {


	private val _conversation = MutableStateFlow(Conversation.ofId(""))
	val conversation = _conversation.asStateFlow()

	private val _conversationJob = MutableStateFlow<Job?>(null)
	val conversationJob = _conversationJob.asStateFlow()

	val settings: StateFlow<Settings> = dataStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Lazily, Settings())

	val currentModelChat = dataStore.settingsFlow.map {
		it.providers.findModelById(it.chatModelId)
	}.stateIn(viewModelScope, SharingStarted.Lazily, null)

	private val settingProviders = dataStore.settingsFlow.map {
		it.providers
	}.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

	private val _errorFlow = MutableSharedFlow<Throwable>()
	val errorFlow = _errorFlow.asSharedFlow()

	fun loadConversation(id: String) {
		viewModelScope.launch(Dispatchers.IO) {
			conversationRepository.getConversationById(id)?.let { conversation ->
				_conversation.update {
					conversation
				}
			}
		}
	}

	fun handleMessageSend(content: List<UIMessagePart>) {
		if (content.isEmptyMessage()) return
		this._conversationJob.value?.cancel()
		val job = viewModelScope.launch(Dispatchers.IO) {
			_conversation.update {
				it.copy(
					messages = it.messages + UIMessage(
						role = MessageRole.USER, parts = content
					), updateAt = Clock.System.now()
				)
			}
			handleMessageComplete()
		}
		_conversationJob.update { job }
		job.invokeOnCompletion {
			_conversationJob.update { null }
		}
	}

	fun setChatModel(modelFromProvider: ModelFromProvider) {
		viewModelScope.launch {
			dataStore.update(
				settings.value.copy(
					chatModelId = modelFromProvider.modelId
				)
			)
		}
	}

	fun handleMessageEdit(uuid: String, parts: List<UIMessagePart>) {
		if (parts.isEmptyMessage()) return
		_conversation.updateAndGet { conversation ->
			conversation.copy(
				messages = conversation.messages.map {
					if (it.id == uuid) {
						it.copy(
							parts = parts
						)
					} else {
						it
					}
				}, updateAt = Clock.System.now()
			)
		}.also { newConversation ->
			val message = newConversation.messages.find { it.id == uuid } ?: return
			regenerateAtMessage(message)
		}
	}

	private suspend fun handleMessageComplete() {
		val model = currentModelChat.value ?: run {
			_errorFlow.emit(IllegalArgumentException("Model not found"))
			return
		}
		runCatching {
			generationHandler.streamText(
				settings.value, model = model, messages = _conversation.value.messages
			).collect {
				updateConversation(conversation.value.copy(messages = it))
			}
		}.onFailure {
			_errorFlow.emit(it)
			_conversationJob.update { null }
		}.onSuccess {
			// ToDo generate title
			saveConversation()
			delay(2000L)
			generateTitle()
		}
	}


	private fun updateConversation(conversation: Conversation) {
		if (conversation.id != _conversation.value.id) return
		_conversation.update { conversation }
	}

	fun regenerateAtMessage(message: UIMessage) {
		if (message.role == MessageRole.USER) {
			val indexAt = conversation.value.messages.indexOf(message)
			indexAt.takeIf { it > -1 }?.let { index ->
				_conversation.updateAndGet { conversation ->
					conversation.copy(
						messages = conversation.messages.subList(0, index + 1)
					)
				}.also {
					// ToDo save the conversation into DB
					saveConversation()
				}
			}
		} else {
			var indexAt = conversation.value.messages.indexOf(message).takeIf { it > -1 }
			indexAt?.let { index ->
				for (i in index downTo 0) {
					if (conversation.value.messages[i].role == MessageRole.USER) {
						indexAt = i
						break
					}
				}
				_conversation.updateAndGet {
					it.copy(messages = it.messages.subList(0, index + 1))
				}.also {
					// ToDo save the conversation into DB
					saveConversation()
				}
			}
		}
		_conversationJob.value?.cancel()
		val job = viewModelScope.launch {
			handleMessageComplete()
		}
		_conversationJob.update { job }
		job.invokeOnCompletion {
			_conversationJob.update { null }
		}
	}

	fun generateTitle() {
		if (_conversation.value.title.isNotBlank()) return
		val savedModelChatId = settings.value.chatModelId
		val provider = settings.value.providers
		if (savedModelChatId.isBlank()) return
		val savedModel = provider.findModelById(savedModelChatId) ?: error("Empty model chat id")
		viewModelScope.launch {
			try {
				val model = savedModel.findProvider(provider)
				val providerHandler = ProviderManager.getProviderByType(model ?: return@launch)
				val result = providerHandler.generateText(
					providerSetting = model,
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
							${_conversation.value.messages.joinToString("\n\n") { it.summaryAsText() }}
							</content>

						""".trimIndent()
						)
					), params = TextGenerationParams(
						model = savedModel, temperature = 0.3f
					)
				)
				val titleIfSuccess = result.choices.firstOrNull()
				if (titleIfSuccess == null) {
					_errorFlow.emit(IllegalArgumentException("Generate title failed"))
				} else {
					titleIfSuccess.message?.toText().takeIf { !it.isNullOrBlank() }?.let { title ->
						_conversation.update {
							it.copy(title = title)
						}
						saveConversation()
					}
				}
			} catch (
				ex: Exception
			) {
				ex.printStackTrace()
			}
		}
	}

	private fun saveConversation() {
		val currentConversation = _conversation.value
		viewModelScope.launch(Dispatchers.IO) {
			val isConversationExist = conversationRepository.getConversationById(currentConversation.id) != null
			try {
				if (isConversationExist) {
					conversationRepository.updateConversation(currentConversation)
				} else {
					conversationRepository.insertConversation(currentConversation)
				}
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}
	}
}