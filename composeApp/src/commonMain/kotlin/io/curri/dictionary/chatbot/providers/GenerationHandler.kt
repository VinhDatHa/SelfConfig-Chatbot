package io.curri.dictionary.chatbot.providers

import io.curri.dictionary.chatbot.data.models.Assistant
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.handleMessageChunk
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.transformer.MessageTransformer
import io.curri.dictionary.chatbot.transformer.transforms
import io.curri.dictionary.chatbot.utils.MockData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GenerationHandler {

	fun streamText(
//		settings: Settings,
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
	): Flow<List<UIMessage>> = flow {
		var messages: List<UIMessage> = messages
		val provider = ProviderSetting.TogetherAiProvider(
			id = "together_ai",
			name = "Together",
			baseUrl = "https://api.together.xyz/v1",
			apiKey = "",
			models = MockData.mockListModel
		) as ProviderSetting

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
			stream = false
		)

//		for (i in 0..maxSteps) {
//			generateInternal(
//				null,
//				messages,
//				{
//					messages = it
//					emit(messages)
//				},
//				transformers,
//				model = model,
//				provider = provider,
//				providerImpl = providerImpl,
////				toolsInternal,
////				memories?.invoke() ?: emptyList(),
//				stream = false
//			)
//		}
	}

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
		stream: Boolean = false
	) {
		val internalMessages = buildList {
			if (assistant != null) {
				val system = buildString {
					// 如果助手有系统提示，则添加到消息中
					if (assistant.systemPrompt.isNotBlank()) {
						append(assistant.systemPrompt)
					}

//					if (assistant.enableMemory) {
//						append(buildMemoryPrompt(memories))
//					}
				}
				if(system.isNotBlank()) add(UIMessage.system(system))
			}
			addAll(messages)
		}.transforms(transformers, model)

		var messages: List<UIMessage> = messages
		val params = TextGenerationParams(
			model = model,
			temperature = assistant?.temperature,
//			tools = tools
		)
//		if (stream) {
//			providerImpl.streamText(
//				providerSetting = provider,
//				messages = internalMessages,
//				params = params
//			).collect {
//				messages = messages.handleMessageChunk(it)
//				onUpdateMessages(messages)
//			}
//		} else {
//			messages = messages.handleMessageChunk(
//				providerImpl.generateText(
//					providerSetting = provider,
//					messages = internalMessages,
//					params = params,
//				)
//			)
//			onUpdateMessages(messages)
//		}
		messages = messages.handleMessageChunk(
			providerImpl.generateText(
				providerSetting = provider,
				messages = internalMessages,
				params = params,
			)
		)
		onUpdateMessages(messages)
	}
}