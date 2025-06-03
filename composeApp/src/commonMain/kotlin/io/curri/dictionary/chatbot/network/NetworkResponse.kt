package io.curri.dictionary.chatbot.network

import kotlinx.serialization.Serializable

@Serializable
sealed class NetworkResponse<out T> {
	class Success<T>(val data: T) : NetworkResponse<T>()
	class Error<T>(val code: Int, val message: String) : NetworkResponse<T>()
	class Exception<T>(val e: Throwable) : NetworkResponse<T>()
}

suspend fun <T : Any> NetworkResponse<T>.onSuccess(
	executable: suspend (T) -> Unit,
) = apply {
	if (this is NetworkResponse.Success) {
		executable(this.data)
	}
}

suspend fun <T : Any> NetworkResponse<T>.onError(
	executable: suspend (code: Int, message: String) -> Unit,
) = apply {
	if (this is NetworkResponse.Error) {
		executable(this.code, this.message)
	}
}

suspend fun <T : Any> NetworkResponse<T>.onException(
	executable: suspend (ex: Throwable) -> Unit,
) = apply {
	if (this is NetworkResponse.Exception) {
		executable(this.e)
	}
}