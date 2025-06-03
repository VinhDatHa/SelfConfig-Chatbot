package io.curri.dictionary.chatbot.app

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform