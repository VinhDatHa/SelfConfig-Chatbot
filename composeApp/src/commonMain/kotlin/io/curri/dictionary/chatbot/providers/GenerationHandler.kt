package io.curri.dictionary.chatbot.providers

import io.curri.dictionary.chatbot.data.data_store.Settings
import io.curri.dictionary.chatbot.data.data_store.findProvider
import io.curri.dictionary.chatbot.data.models.Assistant
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.handleMessageChunk
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.transformer.MessageTransformer
import io.curri.dictionary.chatbot.transformer.transforms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GenerationHandler {

	fun streamText(
		settings: Settings,
		model: ModelFromProvider,
		messages: List<UIMessage>,
		transformers: List<MessageTransformer> = emptyList(),
//		assistant: (() -> Assistant)? = null,
//		memories: (suspend () -> List<AssistantMemory>)? = null,
//		tools: List<Tool> = emptyList(),
//		onCreationMemory: (suspend (String) -> AssistantMemory)? = null,
//		onUpdateMemory: (suspend (Int, String) -> AssistantMemory)? = null,
//		onDeleteMemory: (suspend (Int) -> Unit)? = null,
		maxSteps: Int = 5,
		params: TextGenerationParams? = null,
	): Flow<List<UIMessage>> = flow {
		var messages: List<UIMessage> = messages
		val provider = model.findProvider(settings.providers) ?: error("Provider not found")
		val providerImpl = ProviderManager.getProviderByType(provider)
		generateInternal(
			null,
			messages,
			{
				messages = it
				emit(messages)
			},
			transformers,
			model = model,
			provider = provider,
			providerImpl = providerImpl,
//				toolsInternal,
//				memories?.invoke() ?: emptyList(),
			params = params
		)
		emit(messages)
	}.flowOn(Dispatchers.IO)

	private suspend fun generateInternal(
		assistant: Assistant?,
		messages: List<UIMessage>,
		onUpdateMessages: suspend (List<UIMessage>) -> Unit,
		transformers: List<MessageTransformer>,
		model: ModelFromProvider,
		provider: ProviderSetting,
		providerImpl: Provider<ProviderSetting>,
//		tools: List<Tool>,
//		memories: List<AssistantMemory>,
		stream: Boolean = false,
		params: TextGenerationParams? = null
	) {
		val internalMessages = buildList {
			if (assistant != null) {
				val system = buildString {
					// If the assistant has a system prompt, add it to the message
					if (assistant.systemPrompt.isNotBlank()) {
						append(assistant.systemPrompt)
					}

//					if (assistant.enableMemory) {
//						append(buildMemoryPrompt(memories))
//					}
				}
				if (system.isNotBlank()) add(UIMessage.system(system))
			}
			addAll(messages)
		}.transforms(transformers, model)

		var messages: List<UIMessage> = messages
		val textParam = params ?: TextGenerationParams(
			model = model,
			temperature = assistant?.temperature,
//			tools = tools
		)
		messages = messages.handleMessageChunk(
			providerImpl.generateText(
				providerSetting = provider,
				messages = internalMessages,
				params = textParam,
			)
		)
		onUpdateMessages(messages)
	}
}