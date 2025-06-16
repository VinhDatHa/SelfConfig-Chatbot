@file:OptIn(ExperimentalForeignApi::class)

package io.curri.dictionary.chatbot.data.data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun setupDataStore(): DataStore<Preferences> = createDataStore(
	producePath = {
		val documentDirectory: NSURL = NSFileManager.defaultManager.URLForDirectory(
			directory = NSDocumentDirectory,
			inDomain = NSUserDomainMask,
			appropriateForURL = null,
			create = false,
			error = null,
		) ?: error("Document directory null")
		(documentDirectory.path + "/datastore.preferences_pb")
	}
)