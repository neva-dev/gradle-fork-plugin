package com.neva.gradle.fork

import com.neva.gradle.fork.encryption.Encryption
import org.gradle.api.Project

open class PropsExtension(private val project: Project) {

  internal val encryptor = Encryption.of(project)

  fun named(name: String): String? {
    val value = project.findProperty(name)?.toString()
    if (value.isNullOrBlank()) {
      return value
    }

    return encryptor.decrypt(value)
  }

  operator fun get(name: String) = named(name)

  companion object {

    const val NAME = "props"

    const val ALIAS = "forkProps"
  }
}
