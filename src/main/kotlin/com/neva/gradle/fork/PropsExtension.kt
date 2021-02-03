package com.neva.gradle.fork

import com.neva.gradle.fork.config.properties.PropertyType
import com.neva.gradle.fork.encryption.Encryption
import nu.studer.java.util.OrderedProperties
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import java.io.File

open class PropsExtension(private val project: Project) {

  internal val encryptor by lazy { Encryption.of(project) }

  private val encrypted get() = project.extensions.findByType(ForkExtension::class.java)
      ?.propertyDefinitions?.all.orEmpty().asSequence()
      .filter { it.type == PropertyType.PASSWORD }.map { it.name }.toList()

  fun read(file: File): Map<String, String?> {
    val properties = OrderedProperties().apply { file.inputStream().use { load(it.bufferedReader()) } }
    return properties.entrySet().map { (k, v) -> k to decrypt(k, v) }.toMap()
  }

  operator fun get(name: String): String? {
    val value = project.findProperty(name)?.toString()
    if (value.isNullOrBlank()) {
      return value
    }

    return decrypt(name, value)
  }

  private fun decrypt(name: String, value: String?) = when {
    value.isNullOrBlank() -> value
    encrypted.contains(name) || isEncrypted(value) -> encryptor.decrypt(value)
    else -> value
  }

  private fun isEncrypted(text: String): Boolean {
    return FilenameUtils.wildcardMatch(text, "{*=}")
  }

  companion object {

    const val NAME = "props"

    const val ALIAS = "forkProps"

    fun of(project: Project) = project.extensions.getByType(PropsExtension::class.java)
  }
}

val Project.props get() = PropsExtension.of(this)
