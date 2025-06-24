package io.curri.dictionary.chatbot.data.database.utils

import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.database.entity.ConversationEntity
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.network.decodeFromJson
import io.curri.dictionary.chatbot.network.toJson
import kotlin.time.Instant


fun Conversation.toEntity(): ConversationEntity {
	return ConversationEntity(
		id = id,
		title = title,
		messages = messages.toJson(),
		updateAt = updateAt.toEpochMilliseconds(),
		createAt = createAt.toEpochMilliseconds()
	)
}

fun ConversationEntity.fromEntity(): Conversation {
	return Conversation(
		id = id,
		title = title,
		messages = messages.decodeFromJson<List<UIMessage>>() ?: emptyList(),
		updateAt = Instant.fromEpochMilliseconds(updateAt),
		createAt = Instant.fromEpochMilliseconds(createAt)
	)
}