@file:OptIn(ExperimentalTime::class)

package io.curri.dictionary.chatbot.components.ui

import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Conversation(
	val id: String = Uuid.random().toString(),
	val title: String = "",
	val messages: List<UIMessage>,
	@Serializable(with = InstantSerializer::class)
	val createAt: Instant = Clock.System.now(),
	@Serializable(with = InstantSerializer::class)
	val updateAt: Instant = Clock.System.now()
) {
	// ToDo processImage later

	/*
		val files: List<Uri>
			get() = messages.flatMap { it.parts }
					.filterIsInstance<UIMessagePart.Image>()
					.map { it.url.toUri() }
	 */

	companion object {
		fun empty() = Conversation(messages = emptyList())

		fun ofId(id: String) = Conversation(id, messages = emptyList())

		fun ofUser(prompt: String) = Conversation(
			messages = listOf(
				UIMessage(
					role = MessageRole.USER,
					parts = listOf((UIMessagePart.Text(prompt)))
				)
			)
		)
	}
}

@OptIn(ExperimentalTime::class)
object InstantSerializer : KSerializer<Instant> {
	override val descriptor: SerialDescriptor
		get() = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Instant {
		val isoString = decoder.decodeString()
		return Instant.parse(isoString)
	}

	override fun serialize(encoder: Encoder, value: Instant) {
		val isoString = value.toString()
		encoder.encodeString(isoString)
	}
}