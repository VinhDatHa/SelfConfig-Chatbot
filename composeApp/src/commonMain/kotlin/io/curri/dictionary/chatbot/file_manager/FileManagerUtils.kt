package io.curri.dictionary.chatbot.file_manager

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile


@Composable
expect fun rememberShareManager(): FileManagerUtils

expect class FileManagerUtils {
	fun getFileInBase64(uri: String): String
	suspend fun saveImageToFile(uri: String): String
	fun deleteFile(uris: List<String>)
}