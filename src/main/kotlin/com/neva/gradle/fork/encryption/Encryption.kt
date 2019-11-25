package com.neva.gradle.fork.encryption

import com.neva.gradle.fork.ForkException
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Credentials encryption. Implementation based on Gradle Credentials Plugin algorithm.
 *
 * @link https://github.com/etiennestuder/gradle-credentials-plugin/blob/master/src/main/groovy/nu/studer/gradle/credentials/domain/Encryption.java
 */
internal class Encryption private constructor(private val ecipher: Cipher, private val dcipher: Cipher) {

  private fun encode(bytes: ByteArray): String = BASE64.encodeToString(bytes)

  private fun decode(string: String): ByteArray = BASE64.decode(string)

  @Suppress("TooGenericExceptionCaught")
  fun encrypt(text: String?): String? {
    if (text.isNullOrBlank() || isEncrypted(text)) {
      return text
    }

    try {
      val utf8 = text.toByteArray(charset(CHARSET))
      val enc = ecipher.doFinal(utf8)
      return "{$FORK_PASSWORD_PREFIX${encode(enc)}}"
    } catch (e: Exception) {
      throw ForkException("Encryption failed", e)
    }
  }

  private fun isEncrypted(text: String): Boolean {
    return FilenameUtils.wildcardMatch(text, "{$FORK_PASSWORD_PREFIX*=}")
  }

  @Suppress("TooGenericExceptionCaught")
  fun decrypt(text: String?): String? {
    if (text.isNullOrBlank() || !isEncrypted(text)) {
      return text
    }

    try {
      val raw = text.removeSurrounding("{$FORK_PASSWORD_PREFIX", "}")
      val dec = decode(raw)
      val utf8 = dcipher.doFinal(dec)
      return String(utf8, charset(CHARSET))
    } catch (e: Exception) {
      throw ForkException("Decryption failed", e)
    }
  }

  companion object {

    private val BASE64 = org.apache.commons.codec.binary.Base64(0, byteArrayOf('\r'.toByte(), '\n'.toByte()), false)

    private val CHARSET = "UTF8"

    private const val FORK_PASSWORD_PREFIX = "fp:"

    @Suppress("MagicNumber")
    internal fun of(passphrase: CharArray): Encryption {
      // define a salt to prevent dictionary attacks (ideally, the salt would be
      // regenerated each time and stored alongside the encrypted text)
      val salt = byteArrayOf(0x1F.toByte(), 0x13.toByte(), 0xE5.toByte(), 0xB2.toByte(), 0x49.toByte(), 0x2C.toByte(), 0xC3.toByte(), 0x3C.toByte())

      // use a high iteration count to slow down the decryption speed
      val iterationCount = 65536

      // use the maximum key length that does not require to install the JRE Security Extension
      val keyLength = 128

      // provide password, salt, iteration count, and key length for generating the PBEKey
      val pbeKeySpec = PBEKeySpec(passphrase, salt, iterationCount, keyLength)

      // create a secret (symmetric) key using PBE with SHA1 and AES
      val keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val tmpKey = keyFac.generateSecret(pbeKeySpec)
      val pbeKey = SecretKeySpec(tmpKey.encoded, "AES")

      // create a fixed iv spec that can be used both for encryption and for later decryption
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      val blockSize = cipher.blockSize
      val iv = ByteArray(blockSize)
      for (i in iv.indices) {
        iv[i] = i.toByte()
      }
      val ivSpec = IvParameterSpec(iv)

      // initialize the encryption cipher
      val pbeEcipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      pbeEcipher.init(Cipher.ENCRYPT_MODE, pbeKey, ivSpec)

      // initialize the decryption cipher
      val pbeDcipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      pbeDcipher.init(Cipher.DECRYPT_MODE, pbeKey, ivSpec)

      return Encryption(pbeEcipher, pbeDcipher)
    }

    internal fun of(project: Project): Encryption {
      return of((project.findProperty("fork.encryption.passphrase")?.toString()
        ?: "<<Default passphrase to encrypt passwords!>>").toCharArray())
    }
  }
}
