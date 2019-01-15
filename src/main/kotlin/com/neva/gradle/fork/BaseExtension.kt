package com.neva.gradle.fork

import com.neva.gradle.fork.encryption.Encryption
import org.gradle.api.Project

/**
 * Allows to access encrypted properties.
 */
open class BaseExtension(val project: Project) {

  internal val encryptor = Encryption.of(project)

  fun property(name: String): String? {
    val value = project.findProperty(name)?.toString()
    if (value.isNullOrBlank()) {
      return value
    }

    return encryptor.decrypt(value)
  }

  companion object {

    const val NAME = "fork"

  }

}
