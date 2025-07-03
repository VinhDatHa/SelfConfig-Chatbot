package io.curri.dictionary.chatbot.file_manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

@Composable
actual fun rememberShareManager(): FileManagerUtils {
	return remember { FileManagerUtils() }
}

actual class FileManagerUtils {
	actual fun getFileInBase64(uri: String): String {
		try {
			// Load image from path
			val image = UIImage.imageWithContentsOfFile(uri)
			println("Uri: $uri")
			image?.let {
				val imageData = UIImageJPEGRepresentation(it, 100.0)
				imageData?.base64EncodedStringWithOptions(0.toULong())
			} ?: ""
		} catch (e: Exception) {
			println("Error encoding image to base64: ${e.message}")
		}
		return ""
	}

	@OptIn(ExperimentalForeignApi::class)
	actual suspend fun saveImageToFile(uri: String): String = withContext(Dispatchers.Default) {
		try {
			// Load image from path
			println("URI: $uri")

			val image = UIImage.imageWithContentsOfFile(uri)
				?: error("Cannot load image from path: $uri")

			// Convert to JPEG data
			val imageData = UIImageJPEGRepresentation(image, 0.8) // 80% quality
				?: error("Cannot load image from path: $uri")

			// Get documents directory
			val documentsPath = NSSearchPathForDirectoriesInDomains(
				NSDocumentDirectory,
				NSUserDomainMask,
				true
			).firstOrNull() as? String ?: throw RuntimeException("Cannot access documents directory")

			val imagesDir = (documentsPath as NSString).stringByAppendingPathComponent("saved_images")

			// Create directory if needed
			val fileManager = NSFileManager.defaultManager
			if (!fileManager.fileExistsAtPath(imagesDir)) {
				fileManager.createDirectoryAtPath(
					imagesDir,
					withIntermediateDirectories = true,
					attributes = null,
					error = null
				)
			}

			// Save with unique filename
			val filename = "image_${NSUUID().UUIDString}.jpg"
			val destPath = (imagesDir as NSString).stringByAppendingPathComponent(filename)

			val success = imageData.writeToFile(destPath, atomically = true)
			if (!success) {
				throw RuntimeException("Failed to write image to file")
			}

			return@withContext destPath
		} catch (e: Exception) {
			""
			throw RuntimeException("Failed to save image: ${e.message}", e)
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	actual fun deleteFile(uris: List<String>) {
		try {
			val fileManager = NSFileManager.defaultManager
			val successfullyDeleted = mutableListOf<String>()
			uris.forEach { filePath ->
				try {
					if (fileManager.fileExistsAtPath(filePath)) {
						val success = fileManager.removeItemAtPath(filePath, error = null)
						if (success) {
							println("Successfully deleted: $filePath")
						} else {
							println("Failed to delete: $filePath")
						}
					} else {
						println("File does not exist: $filePath")
					}
				} catch (e: Exception) {
					println("Error deleting file $filePath: ${e.message}")
				}
			}
		} catch (e: Exception) {
			throw RuntimeException("Failed to save image: ${e.message}", e)
		}
	}
}