package io.curri.dictionary.chatbot.transformer

import io.curri.dictionary.chatbot.data.models.Assistant
import io.curri.dictionary.chatbot.data.models.AssistantMemory
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting

class MessageGenerator {

	private suspend fun generateInternal(
		assistant: Assistant?,
		messages: List<UIMessage>,
		onUpdateMessages: suspend (List<UIMessage>) -> Unit,
		transformers: List<MessageTransformer>,
		model: ModelFromProvider,
		provider: ProviderSetting,
		memories: List<AssistantMemory>,
		stream: Boolean = false
	) {

	}
}