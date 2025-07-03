package io.curri.dictionary.chatbot.data.models.serializer

import io.curri.dictionary.chatbot.data.models.ModelType
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

object ModelTypeSerializer : KSerializer<ModelType> {
	override val descriptor: SerialDescriptor
		get() = ListSerializer(ModelType.serializer()).descriptor

	override fun deserialize(decoder: Decoder): ModelType {
		val json = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content
		return ModelType.entries.firstOrNull() { it.name == json.uppercase() } ?: ModelType.UNKNOWN
	}

	@OptIn(InternalSerializationApi::class)
	override fun serialize(encoder: Encoder, value: ModelType) {
		encoder.encodeSerializableValue(ModelType::class.serializer(), value)
	}
}