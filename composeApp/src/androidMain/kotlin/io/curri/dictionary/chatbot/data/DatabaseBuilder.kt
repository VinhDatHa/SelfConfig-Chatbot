package io.curri.dictionary.chatbot.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.curri.dictionary.chatbot.data.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
	val appContext = context.applicationContext
	val dbFile = appContext.getDatabasePath("conversation_database.db")

	return Room.databaseBuilder<AppDatabase>(
		context = appContext,
		name = dbFile.absolutePath,
	)
}