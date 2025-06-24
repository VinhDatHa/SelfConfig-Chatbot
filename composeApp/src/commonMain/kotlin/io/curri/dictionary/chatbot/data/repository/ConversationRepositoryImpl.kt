package io.curri.dictionary.chatbot.data.repository

import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.database.dao.ConversationDao
import io.curri.dictionary.chatbot.data.database.utils.fromEntity
import io.curri.dictionary.chatbot.data.database.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConversationRepositoryImpl(
	private val conversationDao: ConversationDao
) : ConversationRepository {
	override fun getAllConversation(): Flow<List<Conversation>> {
		return conversationDao.getAll().map { flow ->
			flow.map {
				it.fromEntity()
			}
		}
	}

	override suspend fun getConversationById(uuid: String): Conversation? {
		return conversationDao.getConversationById(uuid)?.fromEntity()
	}

	override fun getConversationByTitle(title: String): Flow<List<Conversation>> {
		return conversationDao.searchConversations(title).map { flow ->
			flow.map { it.fromEntity() }
		}
	}

	override suspend fun insertConversation(conversation: Conversation) {
		conversationDao.insert(conversation.toEntity())
	}

	override suspend fun updateConversation(conversation: Conversation) {
		conversationDao.update(conversation.toEntity())
	}

	override suspend fun deleteConversation(conversation: Conversation) {
		conversationDao.delete(conversation.toEntity())
	}

	override suspend fun deleteAllConversation() {
		conversationDao.deleteAll()
	}

}