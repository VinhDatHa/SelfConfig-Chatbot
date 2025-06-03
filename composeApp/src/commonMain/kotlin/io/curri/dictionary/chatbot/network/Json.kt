package io.curri.dictionary.chatbot.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

val jsonConfig = Json {
	ignoreUnknownKeys = true
	useAlternativeNames = false
	allowSpecialFloatingPointValues = true
	prettyPrint = true
	isLenient = true
	coerceInputValues = true
	encodeDefaults = true
	explicitNulls = false
	classDiscriminator = "classType"
}

inline fun <reified T> T.toJson(): String = jsonConfig.encodeToString<T>(this)

inline fun <reified T> String.decodeFromJson(): T? = try {
	jsonConfig.decodeFromString<T>(this)
} catch (ex: SerializationException) {
	null
}