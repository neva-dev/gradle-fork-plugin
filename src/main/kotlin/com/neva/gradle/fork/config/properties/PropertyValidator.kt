package com.neva.gradle.fork.config.properties

class PropertyValidator(val value: String) {

  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()
}
