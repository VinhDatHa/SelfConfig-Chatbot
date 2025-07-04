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
import io.curri.dictionary.chatbot.file_manager.FileManagerUtils
import io.curri.dictionary.chatbot.network.search.BingSearchService
import io.curri.dictionary.chatbot.network.search.SearchCommonOptions
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.uuid.Uuid

class ChatVM(
	savedStateHandle: SavedStateHandle,
	private val dataStore: DataStoreManager,
	private val generationHandler: GenerationHandler,
	private val conversationRepository: ConversationRepository,
	private val fileManagerUtils: FileManagerUtils
) : ViewModel() {

	private val _conversation = MutableStateFlow(Conversation.ofId(Uuid.random().toString()))
	val conversation = _conversation.asStateFlow()

	private val _conversationJob = MutableStateFlow<Job?>(null)
	val conversationJob = _conversationJob.asStateFlow()

	val settings: StateFlow<Settings> = dataStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Lazily, Settings())

	val currentModelChat = dataStore.settingsFlow.map {
		it.providers.findModelById(it.chatModelId)
	}.stateIn(viewModelScope, SharingStarted.Lazily, null)

	private val _allConversations = MutableStateFlow(emptyList<Conversation>())
	val allConversations: StateFlow<List<Conversation>>
		get() = _allConversations.asStateFlow()

	private val _errorFlow = MutableSharedFlow<Throwable>()
	val errorFlow = _errorFlow.asSharedFlow()

	fun loadConversation(id: String) {
		viewModelScope.launch(Dispatchers.IO) {
			conversationRepository.getConversationById(id)?.let { current ->
				_conversation.update { current }
			}

			conversationRepository.getAllConversation().collectLatest { history ->
				_allConversations.update { history }
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

	private suspend fun handleWebSearch() {
		val search = BingSearchService
		withContext(Dispatchers.IO) {
			val result = search.search(
				query = _conversation.value.messages.last().toText(),
				options = SearchCommonOptions(5)
			)
			result.onSuccess { searchResult ->
				_conversation.update { current ->
					current.copy(
						messages = current.messages + UIMessage(
							role = MessageRole.ASSISTANT,
							parts = listOf(
								UIMessagePart.Search(searchResult)
							)
						)
					)
				}
			}.onFailure {
				it.printStackTrace()
			}
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
				settings = settings.value,
				model = model,
				messages = _conversation.value.messages
			).collect { messages ->
				_conversation.updateAndGet {
					it.copy(messages = messages)
				}.let {
					saveConversation(it)
				}
			}
		}.onFailure {
			_errorFlow.emit(it)
			_conversationJob.update { null }
		}.onSuccess {
			delay(1000L)
			generateTitle()
		}
	}

	private fun checkFileDelete(newConversation: Conversation, oldConversation: Conversation) {
		val newFiles = newConversation.files
		val oldFiles = oldConversation.files
		val deletedFiles = oldFiles.filter { file ->
			newFiles.none { it == file }
		}
		if (deletedFiles.isNotEmpty()) {
			fileManagerUtils.deleteFile(deletedFiles)
		}
	}

	fun deleteImage(uris: List<String>) {
		fileManagerUtils.deleteFile(uris)
	}

	private fun updateConversation(conversation: Conversation) {
		if (conversation.id != _conversation.value.id) return
		checkFileDelete(conversation, _conversation.value)
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
					saveConversation(it)
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
					saveConversation(it)
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
		if (_conversation.value.title.isNotBlank() || _conversation.value.title != "..." || currentModelChat.value == null) return
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
					titleIfSuccess.message?.toText()?.trim()?.let { title ->
						_conversation.updateAndGet { it.copy(title = title) }.let {
							saveConversation(it)
						}
					}
				}
			} catch (
				ex: Exception
			) {
				ex.printStackTrace()
			}
		}
	}

	private fun saveConversation(conversation: Conversation) {
		viewModelScope.launch(Dispatchers.IO) {
			val isConversationExist = conversationRepository.getConversationById(conversation.id) != null
			try {
				if (isConversationExist) {
					conversationRepository.updateConversation(conversation)
				} else {
					conversationRepository.insertConversation(conversation)
				}
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}
	}
}