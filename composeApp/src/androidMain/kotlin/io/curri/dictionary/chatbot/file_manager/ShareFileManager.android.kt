package io.curri.dictionary.chatbot.file_manager

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.uuid.Uuid

@Composable
actual fun rememberShareManager(): FileManagerUtils {
	val context = LocalContext.current
	return remember { FileManagerUtils(context) }
}

actual class FileManagerUtils(private val context: Context) {
	actual fun getFileInBase64(uri: String): String {
		return try {
			val type = context.contentResolver.getType(uri.toUri()) ?: "image/jpg"
			val bytes = context.contentResolver.openFileDescriptor(uri.toUri(), "r")?.use { pfd ->
				FileInputStream(pfd.fileDescriptor).use { it.readBytes() }
			}
			val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

			"data:${type};base64,${base64}"
		} catch (ex: Exception) {
			ex.printStackTrace()
			""
		}
	}

	actual suspend fun saveImageToFile(uri: String, kmpFile: KmpFile?): String {
		return try {
			val fileDir = context.filesDir.resolve("images")
			if (!fileDir.exists()) {
				fileDir.mkdirs()
			}
			val fileName = Uuid.random().toString()
			val targetUri = fileDir.resolve(fileName).toUri()
			withContext(Dispatchers.Default) {
				context.contentResolver.openInputStream(uri.toUri())?.use { inStream ->
					context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
						inStream.copyTo(outStream)
					}
				}
				targetUri.toString()
			}
		} catch (ex: Exception) {
			ex.printStackTrace()
			""
		}
	}

	actual fun deleteFile(uris: List<String>) {
		uris.map { it.toUri() }.forEach {
			val file = it.toFile()
			if (file.exists()) file.delete()
		}
	}
}

private fun File.isSupportedType(): String {
	val mimeType = guessMimeType().getOrNull() ?: return ""
	return mimeType
}

private fun File.guessMimeType(): Result<String> = try {
	inputStream().use { input ->
		val bytes = ByteArray(16)
		val read = input.read(bytes)
		if (read < 12) error("File too short to determine MIME type")
		println("guessMimeType bytes = ${bytes.joinToString(",")}")

		if (bytes.copyOfRange(4, 12).toString(Charsets.US_ASCII) == "ftypheic") {
			return Result.success("image/heic")
		}

		if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
			return Result.success("image/jpeg")
		}

		if (bytes.copyOfRange(0, 8).contentEquals(
				byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
			)
		) {
			return Result.success("image/png")
		}

		val header = bytes.copyOfRange(0, 6).toString(Charsets.US_ASCII)
		if (header == "GIF89a" || header == "GIF87a") {
			return Result.success("image/gif")
		}

		error("Failed to guess MIME type: $header")
	}
} catch (ex: Exception) {
	Result.failure(ex)
}