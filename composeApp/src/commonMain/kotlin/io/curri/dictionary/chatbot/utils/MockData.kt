package io.curri.dictionary.chatbot.utils

import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.Modality
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.ModelType
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object MockData {
	@OptIn(ExperimentalTime::class)
	val mockConversation = Conversation(
		id = "123123",
		title = "AI Assistant Demo Chat",
		messages = listOf(
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.USER,
				parts = listOf(
					UIMessagePart.Text("Hello, how are you doing today?")
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.SYSTEM,
				parts = listOf(
					UIMessagePart.Text("I'm doing great, thank you for asking! How can I assist you today?")
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.USER,
				parts = listOf(
					UIMessagePart.Text("Can you show me a picture of a cute cat and tell me the current weather in New York?")
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.SYSTEM,
				parts = listOf(
					UIMessagePart.Image("https://placekitten.com/300/200"), // A classic cat placeholder
					UIMessagePart.Text("Here's a cute cat for you!"),
					UIMessagePart.Text("Now, let me fetch the weather for New York...")
				)
			),
			// Simulate a tool call and its result
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.TOOL, // Represents the tool's output
				parts = listOf(
					UIMessagePart.ToolResult(
						toolCallId = "weather_call_1",
						toolName = "weather_api",
						content = JsonObject(
							mapOf(
								"location" to JsonPrimitive("New York"),
								"temperature" to JsonPrimitive("25°C"),
								"conditions" to JsonPrimitive("Clear sky"),
								"humidity" to JsonPrimitive("60%")
							)
						),
						arguments = JsonObject(
							mapOf(
								"city" to JsonPrimitive("New York")
							)
						)
					)
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.SYSTEM,
				parts = listOf(
					UIMessagePart.Text("The current weather in New York is 25°C with a clear sky and 60% humidity."),
					UIMessagePart.Text("Is there anything else I can help you with?")
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.USER,
				parts = listOf(
					UIMessagePart.Text("Nope, that's perfect for now. Thanks!")
				)
			),
			UIMessage(
				id = Uuid.random().toString(),
				role = MessageRole.SYSTEM,
				parts = listOf(
					UIMessagePart.Text("You're most welcome! Have a great day!")
				)
			)
		),
		createAt = Clock.System.now().minus(30.minutes), // Conversation started 30 minutes ago
		updateAt = Clock.System.now() // Last updated right now
	)

	val mockModelProvider = ModelFromProvider(
		modelId = "meta-llama/Llama-Vision-Free",
		displayName = "Llama-Vision-Free".replace("-", " "),
		type = ModelType.CHAT
	)

	val mockListModel = listOf(
		ModelFromProvider(
			modelId = "meta-llama/Llama-Vision-Free",
			displayName = "Meta Llama Vision Free",
			type = ModelType.CHAT,
			inputModalities = listOf(Modality.TEXT, Modality.IMAGE)
		),
		ModelFromProvider(
			modelId = "meta-llama/Llama-Guard-3-11B-Vision-Turbo",
			displayName = "Meta Llama Guard 3 11B Vision Turbo",
			type = ModelType.CHAT,
			inputModalities = listOf(Modality.TEXT, Modality.IMAGE)
		),
		ModelFromProvider(
			modelId = "meta-llama/Llama-3.2-3B-Instruct-Turbo",
			displayName = "Meta Llama 3.2 3B Instruct Turbo",
			type = ModelType.CHAT,
			inputModalities = listOf(Modality.TEXT)
		),
		ModelFromProvider(
			modelId = "black-forest-labs/FLUX.1-dev",
			displayName = "FLUX.1 [dev]",
			type = ModelType.IMAGE,
			// Image generation models typically take text as input for the prompt
			inputModalities = listOf(Modality.TEXT)
		),
		ModelFromProvider(
			modelId = "meta-llama/Llama-3.3-70B-Instruct-Turbo",
			displayName = "Meta Llama 3.3 70B Instruct Turbo",
			type = ModelType.CHAT,
			inputModalities = listOf(Modality.TEXT)
		)
	)

}