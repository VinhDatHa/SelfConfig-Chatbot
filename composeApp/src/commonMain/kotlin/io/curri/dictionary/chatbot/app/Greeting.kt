package io.curri.dictionary.chatbot.app

class Greeting {
	private val platform = getPlatform()

	fun greet(): String {
		return "Hello, ${platform.name}!"
	}
}