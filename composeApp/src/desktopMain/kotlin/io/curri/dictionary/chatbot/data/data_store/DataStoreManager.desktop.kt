package io.curri.dictionary.chatbot.data.data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

fun setupDataStore(): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(
		produceFile = {
			getDataStoreFile().absolutePath.toPath()
		}
	)
}

private fun getDataStoreFile(): File {
	val os = System.getProperty("os.name").lowercase()
	val userDir = System.getProperty("user.home")

	val appDataDir = when {
		os.contains("win") -> {
			// Windows: Use APPDATA or user home
			val appData = System.getenv("APPDATA")
			if (appData != null) File(appData, "YourAppName")
			else File(userDir, "AppData/Roaming/YourAppName")
		}

		os.contains("mac") -> {
			// macOS: Use Application Support
			File(userDir, "Library/Application Support/YourAppName")
		}

		else -> {
			// Linux: Use .config directory
			val configHome = System.getenv("XDG_CONFIG_HOME")
			if (configHome != null) File(configHome, "YourAppName")
			else File(userDir, ".config/YourAppName")
		}
	}

	// Create directory if it doesn't exist
	if (!appDataDir.exists()) {
		appDataDir.mkdirs()
	}

	return File(appDataDir, "datastore/settings.preferences_pb")
}