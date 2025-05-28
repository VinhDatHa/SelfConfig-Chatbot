package io.curri.dictionary.chatbot

class Greeting {
	private val platform = getPlatform()

	fun greet(): String {
		return "Hello, ${platform.name}!"
	}
}