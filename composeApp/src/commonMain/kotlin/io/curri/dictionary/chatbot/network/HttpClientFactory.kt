package io.curri.dictionary.chatbot.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

object HttpClientFactory {
	fun create(engine: HttpClientEngine): HttpClient {
		return HttpClient(engine) {
			install(ContentNegotiation) {
				json(jsonConfig)
			}
			install(HttpTimeout) {
				socketTimeoutMillis = 30_000L
				requestTimeoutMillis = 30_000L
				connectTimeoutMillis = 30_000L
			}
			install(HttpRequestRetry) {
				retryOnServerErrors(maxRetries = 5)
				exponentialDelay()
			}
			install(Logging) {
				logger = object : Logger {
					override fun log(message: String) {
						println(message)
					}
				}
				level = LogLevel.ALL
			}
			defaultRequest {
				contentType(ContentType.Application.Json)
			}
		}
	}
}