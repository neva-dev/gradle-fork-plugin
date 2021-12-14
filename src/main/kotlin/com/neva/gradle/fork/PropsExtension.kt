package com.neva.gradle.fork

import com.neva.gradle.fork.encryption.Encryption
import nu.studer.java.util.OrderedProperties
import org.gradle.api.Project
import java.io.File

open class PropsExtension(private val project: Project) {

  internal val encryptor by lazy { Encryption() }

  fun read(file: File): Map<String, String?> {
    val properties = OrderedProperties().apply { file.inputStream().use { load(it.bufferedReader()) } }
    return properties.entrySet().associate { (k, v) -> k to encryptor.decrypt(v, k) }
  }

  operator fun get(name: String): String? {
    val value = project.findProperty(name)?.toString()
    if (value.isNullOrBlank()) {
      return value
    }

    return encryptor.decrypt(value, name)
  }

  companion object {

    const val NAME = "props"

    const val ALIAS = "forkProps"

    fun of(project: Project) = project.extensions.getByType(PropsExtension::class.java)
  }
}

val Project.props get() = PropsExtension.of(this)
