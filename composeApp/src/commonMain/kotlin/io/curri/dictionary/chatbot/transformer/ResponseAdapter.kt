package io.curri.dictionary.chatbot.transformer

import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.UIMessage

interface MessageTransformer {
	fun transform(
		messages: List<UIMessage>,
		model: ModelFromProvider,
	): List<UIMessage>
}

fun List<UIMessage>.transforms(
	transformers: List<MessageTransformer>,
	model: ModelFromProvider
): List<UIMessage> {
	return transformers.fold(this) { acc, transformer ->
		transformer.transform(acc, model)
	}
}