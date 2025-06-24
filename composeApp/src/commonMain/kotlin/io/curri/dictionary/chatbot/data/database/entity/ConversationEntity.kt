package io.curri.dictionary.chatbot.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey

@Entity
data class ConversationEntity(
	@PrimaryKey val id: String,
	@ColumnInfo("title") val title: String,
	@ColumnInfo("messages") val messages: String,
	@ColumnInfo("create_at") val createAt: Long,
	@ColumnInfo("update_at") val updateAt: Long,
)

@Entity
data class MemoryEntity(
	@PrimaryKey(true)
	val id: Int = 0,
	@ColumnInfo("assistant_id")
	val assistantId: String,
	@ColumnInfo("content")
	val content: String = "",
)