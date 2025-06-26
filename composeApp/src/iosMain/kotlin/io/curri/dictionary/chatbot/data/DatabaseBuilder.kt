package io.curri.dictionary.chatbot.data

import androidx.room.ConstructedBy
import androidx.room.Room
import androidx.room.RoomDatabase
import io.curri.dictionary.chatbot.data.database.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask


fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
	val dbFilePath = documentDirectory() + "/movie_database.db"
	return Room.databaseBuilder<AppDatabase>(
		name = dbFilePath,
	)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
	val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
		directory = NSDocumentDirectory,
		inDomain = NSUserDomainMask,
		appropriateForURL = null,
		create = false,
		error = null,
	)

	return requireNotNull(documentDirectory?.path)
}