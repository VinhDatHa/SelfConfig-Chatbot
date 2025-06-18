package io.curri.dictionary.chatbot.presentation.chat_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.data_store.DataStoreManager
import io.curri.dictionary.chatbot.data.data_store.Settings
import io.curri.dictionary.chatbot.data.data_store.findModelById
import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import io.curri.dictionary.chatbot.data.models.isEmptyMessage
import io.curri.dictionary.chatbot.providers.GenerationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
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
import kotlin.uuid.Uuid

class ChatVM(
	savedStateHandle: SavedStateHandle,
	private val dataStore: DataStoreManager,
	private val generationHandler: GenerationHandler,
) : ViewModel() {

	private val _conversationID: String = Uuid.parse(checkNotNull(savedStateHandle["id"] ?: "${Uuid.random()}")).toString()

	private val _conversation = MutableStateFlow(Conversation.ofId(_conversationID))
	val conversation = _conversation.asStateFlow()

	private val _conversationJob = MutableStateFlow<Job?>(null)
	val conversationJob = _conversationJob.asStateFlow()

	val settings: StateFlow<Settings> = dataStore.settingsFlow
		.stateIn(viewModelScope, SharingStarted.Lazily, Settings())

	val currentModelChat = dataStore.settingsFlow.map {
		it.providers.findModelById(it.chatModelId)
	}.stateIn(viewModelScope, SharingStarted.Lazily, null)

	private val _errorFlow = MutableSharedFlow<Throwable>()
	val errorFlow = _errorFlow.asSharedFlow()

	fun handleMessageSend(content: List<UIMessagePart>) {
		if (content.isEmptyMessage()) return
		this._conversationJob.value?.cancel()
		val job = viewModelScope.launch(Dispatchers.IO) {
			/*
			val newConversation = conversation.value.copy(
				messages = conversation.value.messages + UIMessage(
					role = MessageRole.USER,
					parts = content
				),
				updateAt = Clock.System.now()
			)
			// ToDo Save conversation
			 */
			_conversation.update {
				it.copy(
					messages = it.messages + UIMessage(
						role = MessageRole.USER,
						parts = content
					),
					updateAt = Clock.System.now()
				)
			}
			handleMessageComplete()
		}
		_conversationJob.update { job }
		job.invokeOnCompletion {
			this._conversationJob.update { null }
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
				},
				updateAt = Clock.System.now()
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
				settings.value,
				model = model,
				messages = _conversation.value.messages
			).collect {
				it.map { message ->
					message.parts.forEach { part ->
						println("Text ${(part as UIMessagePart.Text).text}")
					}
				}
				updateConversation(conversation.value.copy(messages = it))
			}
		}.onFailure {
			_errorFlow.emit(it)
		}.onSuccess {
			// ToDo generate title
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
					// ToDo Save DB
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

}