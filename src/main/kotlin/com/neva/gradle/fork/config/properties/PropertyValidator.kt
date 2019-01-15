package com.neva.gradle.fork.config.properties

class PropertyValidator(val property: Property) {

  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()

  fun notContains(otherName: String) {
    val otherValue = property.other(otherName).value
    if (otherValue.contains(property.value)) {
      error("Cannot contain value '$otherValue' ($otherName)'")
    }
  }
}
