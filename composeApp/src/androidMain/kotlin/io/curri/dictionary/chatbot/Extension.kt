package io.curri.dictionary.chatbot

import android.net.Uri
import io.curri.dictionary.chatbot.components.chat.ChatInputState
import io.curri.dictionary.chatbot.data.models.UIMessagePart

fun ChatInputState.addImage(uris: List<Uri>) {
	val newMessage = this.messageContent.toMutableList()
	uris.forEach { uri ->
		newMessage.add(UIMessagePart.Image(uri.toString()))
	}
	messageContent = newMessage
}