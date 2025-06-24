package io.curri.dictionary.chatbot.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.curri.dictionary.chatbot.data.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
	@Query("SELECT * FROM conversationentity ORDER BY create_at DESC")
	fun getAll(): Flow<List<ConversationEntity>>

	@Query("SELECT * FROM conversationentity WHERE title LIKE '%' || :searchText || '%' ORDER BY create_at DESC")
	fun searchConversations(searchText: String): Flow<List<ConversationEntity>>

	@Query("SELECT * FROM conversationentity WHERE id = :id")
	fun getConversationFlowById(id: String): Flow<ConversationEntity?>

	@Query("SELECT * FROM conversationentity WHERE id = :id")
	suspend fun getConversationById(id: String): ConversationEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(conversation: ConversationEntity)

	@Update
	suspend fun update(conversation: ConversationEntity)

	@Delete
	suspend fun delete(conversation: ConversationEntity)

	@Query("DELETE FROM conversationentity")
	suspend fun deleteAll()
}