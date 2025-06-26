package io.curri.dictionary.chatbot.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.curri.dictionary.chatbot.data.database.dao.ConversationDao
import io.curri.dictionary.chatbot.data.database.entity.ConversationEntity

@Database(entities = [ConversationEntity::class], version = 1)
@ConstructedBy(AppDatabaseDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getConversationDao(): ConversationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}