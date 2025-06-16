package io.curri.dictionary.chatbot.data.data_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
	name = DATA_STORE_NAME,
	produceMigrations = { context ->
		listOf(
			SharedPreferencesMigration(context, DATA_STORE_NAME)
		)
	}
)

fun setupDataStore(context: Context): DataStore<Preferences> = context.dataStore