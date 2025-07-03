package io.curri.dictionary.chatbot.data.data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.network.jsonConfig
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import okio.Path.Companion.toPath
import kotlin.uuid.Uuid


fun createDataStore(producePath: () -> String): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(
		produceFile = { producePath().toPath() }
	)
}

internal const val DATA_STORE_NAME = "prefs_data_store"

class DataStoreManager(
	private val dataStore: DataStore<Preferences>
) {
	companion object {
		val SELECT_MODEL = stringPreferencesKey("chat_model")
		val TITLE_MODEL = stringPreferencesKey("title_model")
		val PROVIDERS = stringPreferencesKey("providers")
		val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
	}

	val settingsFlow = dataStore.data
		.catch { exception ->
			println("Exception: $exception")
			if (exception is IOException) {
				emit(emptyPreferences())
			} else {
				throw exception
			}
		}
		.map { preferences ->
			Settings(
				chatModelId = preferences[SELECT_MODEL] ?: "${Uuid.random()}",
				titleModelId = preferences[TITLE_MODEL] ?: "${Uuid.random()}",
				providers = jsonConfig.decodeFromString(preferences[PROVIDERS] ?: "[]"),
				dynamicColor = preferences[DYNAMIC_COLOR] != false,
			)

		}.catch {
			it.printStackTrace()
			update(Settings())
			emit(Settings())
		}
		.map {
			val providers = it.providers.ifEmpty { DEFAULT_PROVIDERS }.toMutableList()
			DEFAULT_PROVIDERS.forEach { defaultProvider ->
				if (providers.none { it.id == defaultProvider.id }) {
					providers.add(defaultProvider)
				}
			}
			it.copy(
				providers = providers.distinctBy { it.id }
			)
		}
		.map { settings ->
			// 去重
			settings.copy(
				providers = settings.providers.distinctBy { it.id }.map { provider ->
					when (provider) {
						is ProviderSetting.TogetherAiProvider -> provider.copy(
							models = provider.models.distinctBy { model -> model.modelId }
						)

						is ProviderSetting.OpenAiProvider -> provider.copy(
							models = provider.models.distinctBy { model -> model.modelId }
						)
					}
				}
			)
		}

	suspend fun update(settings: Settings) {
		dataStore.edit { preferences ->
			preferences[DYNAMIC_COLOR] = settings.dynamicColor
			preferences[SELECT_MODEL] = settings.chatModelId
			preferences[TITLE_MODEL] = settings.titleModelId
			preferences[PROVIDERS] = jsonConfig.encodeToString(settings.providers)
//			preferences[TRANSLATE_MODEL] = settings.translateModeId.toString()
//			preferences[ASSISTANTS] = JsonInstant.encodeToString(settings.assistants)
//			preferences[SELECT_ASSISTANT] = settings.assistantId.toString()
//			preferences[SEARCH_SERVICE] = JsonInstant.encodeToString(settings.searchServiceOptions)
//			preferences[SEARCH_COMMON] = JsonInstant.encodeToString(settings.searchCommonOptions)
		}
	}
}