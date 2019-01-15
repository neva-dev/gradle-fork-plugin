package com.neva.gradle.fork.config.properties

class PropertyValidator(val property: Property) {

  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()

  fun shouldNotContain(otherName: String) {
    val otherValue = property.other(otherName).value
    if (property.value.contains(otherValue)) {
      error("Should not contain value '$otherValue' ($otherName)")
    }
  }
}
