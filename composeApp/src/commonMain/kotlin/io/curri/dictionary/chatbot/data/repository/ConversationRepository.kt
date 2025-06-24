package io.curri.dictionary.chatbot.data.repository

import io.curri.dictionary.chatbot.components.ui.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
	fun getAllConversation(): Flow<List<Conversation>>
	fun getConversationByTitle(title: String): Flow<List<Conversation>>
	suspend fun getConversationById(uuid: String): Conversation?
	suspend fun insertConversation(conversation: Conversation)
	suspend fun updateConversation(conversation: Conversation)
	suspend fun deleteConversation(conversation: Conversation)
	suspend fun deleteAllConversation()
}