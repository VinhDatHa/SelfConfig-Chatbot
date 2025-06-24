package io.curri.dictionary.chatbot.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.curri.dictionary.chatbot.data.database.dao.ConversationDao
import io.curri.dictionary.chatbot.data.database.entity.ConversationEntity

@Database(entities = [ConversationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getConversationDao(): ConversationDao
}