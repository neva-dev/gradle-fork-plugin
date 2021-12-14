package com.neva.gradle.fork.encryption

import com.neva.gradle.fork.ForkException
import org.apache.commons.codec.binary.Base64
import java.nio.charset.Charset

internal class Encryption {

  private fun encode(bytes: ByteArray): String = BASE64.encodeToString(bytes)

  private fun decode(string: String): ByteArray = BASE64.decode(string)

  @Suppress("TooGenericExceptionCaught")
  fun encrypt(text: String?, context: String? = null): String? {
    if (text.isNullOrBlank()) {
      return text
    }

    try {
      return "${TOKEN_START}${encode(text.toByteArray(CHARSET))}$TOKEN_END"
    } catch (e: Exception) {
      throw ForkException("Fork property encryption failed! Context: '${context.orEmpty()}'", e)
    }
  }

  @Suppress("TooGenericExceptionCaught")
  fun decrypt(text: String?, context: String? = null): String? {
    if (text.isNullOrBlank() || !isEncrypted(text)) {
      return text
    }

    val raw = text.removeSurrounding(TOKEN_START, TOKEN_END)
    try {
      return decode(raw).toString(CHARSET)
    } catch (e: Exception) {
      throw ForkException(listOf(
        "Fork property decryption failed! Context: '${context.orEmpty()}'",
        "Most probably encrypted value got corrupted.",
        "Consider regenerating encrypted values to fix the problem."
      ).joinToString("\n"), e)
    }
  }

  fun isEncrypted(text: String?): Boolean {
    if (text.isNullOrBlank()) {
      return false
    }

    return text.startsWith(TOKEN_START) && text.endsWith(TOKEN_END)
  }

  companion object {

    private val BASE64 = Base64(0, byteArrayOf('\r'.toByte(), '\n'.toByte()), false)

    private val CHARSET = Charset.forName("UTF8")

    private const val TOKEN_START = "{fp/}"

    private const val TOKEN_END = "{/fp}"
  }
}
