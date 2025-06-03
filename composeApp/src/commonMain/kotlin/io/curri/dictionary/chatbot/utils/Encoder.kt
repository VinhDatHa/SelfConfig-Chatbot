package io.curri.dictionary.chatbot.utils

/*
private val supportedTypes = setOf(
    "image/jpeg",
    "image/png",
    "image/gif",
    "image/webp",
)

fun UIMessagePart.Image.encodeBase64(withPrefix: Boolean = true): Result<String> = runCatching {
    if (this.url.startsWith("file://")) {
        val filePath =
            this.url.toUri().path ?: throw IllegalArgumentException("Invalid file URI: ${this.url}")
        val file = File(filePath)
        if (file.exists()) {
            if(!file.isSupportedType()) {
                convertToJpeg(file)// 转换为 JPEG 格式
                println("File converted to JPEG format: ${file.absolutePath}")
            }
            val bytes = file.readBytes()
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            if(withPrefix) "data:${file.guessMimeType().getOrThrow()};base64,$encoded" else encoded
        } else {
            throw IllegalArgumentException("File does not exist: ${this.url}")
        }
    } else {
        throw IllegalArgumentException("Unsupported URL format: $url")
    }
}

private fun convertToJpeg(file: File) = runCatching {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    }
}

private fun File.isSupportedType(): Boolean {
    val mimeType = guessMimeType().getOrNull() ?: return false
    return mimeType in supportedTypes
}

private fun File.guessMimeType(): Result<String> = runCatching {
    inputStream().use { input ->
        val bytes = ByteArray(16)
        val read = input.read(bytes)
        if (read < 12) error("File too short to determine MIME type")

        // 打印前16个字节（可选）
        println("guessMimeType bytes = ${bytes.joinToString(",")}")

        // 判断 HEIC 格式：包含 "ftypheic"
        if (bytes.copyOfRange(4, 12).toString(Charsets.US_ASCII) == "ftypheic") {
            return@runCatching "image/heic"
        }

        // 判断 JPEG 格式：开头为 0xFF 0xD8
        if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
            return@runCatching "image/jpeg"
        }

        // 判断 PNG 格式：开头为 89 50 4E 47 0D 0A 1A 0A
        if (bytes.copyOfRange(0, 8).contentEquals(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            )
        ) {
            return@runCatching "image/png"
        }

        // 判断 GIF 格式：开头为 "GIF89a" 或 "GIF87a"
        val header = bytes.copyOfRange(0, 6).toString(Charsets.US_ASCII)
        if (header == "GIF89a" || header == "GIF87a") {
            return@runCatching "image/gif"
        }

        error("Failed to guess MIME type: $header")
    }
}
 */