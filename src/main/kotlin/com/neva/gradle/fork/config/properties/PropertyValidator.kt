package com.neva.gradle.fork.config.properties

import java.net.MalformedURLException
import java.net.URL
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class PropertyValidator(val property: Property) {

  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()

  fun checkRegex(regex: String) = Regex(regex).matches(property.value)

  fun notEmpty() {
    if (property.value.isEmpty()) {
      error("Value is required")
    }
  }

  fun notBlank() {
    if (property.value.isBlank()) {
      error("Should not be blank")
    }
  }

  fun regex(regex: String) {
    if (!checkRegex(regex)) {
      error("Should match regex '$regex'")
    }
  }

  fun regex(valueType: String, regex: String) {
    if (!checkRegex(regex)) {
      error("Should be a valid $valueType")
    }
  }

  fun alphanumeric() {
    if (!checkRegex("^[a-zA-Z0-9]+$")) {
      error("Should be alphanumeric")
    }
  }

  fun numeric() {
    if (!checkRegex("^[0-9]+$")) {
      error("Should be numeric")
    }
  }

  fun alpha() {
    if (!checkRegex("^[a-zA-Z0-9]+$")) {
      error("Should contain only alphabetic characters")
    }
  }

  /**
   * @see <https://gist.github.com/rishabhmhjn/8663966>
   */
  fun javaPackage() {
    regex("java package", "^(?:[a-zA-Z]+(?:\\d*[a-zA-Z_]*)*)(?:\\.[a-zA-Z]+(?:\\d*[a-zA-Z_]*)*)+\$")
  }

  fun lowercased() {
    if (property.value.toLowerCase() != property.value) {
      error("Should be lowercased")
    }
  }

  fun uppercased() {
    if (property.value.toUpperCase() != property.value) {
      error("Should be uppercased")
    }
  }

  fun capitalized() {
    if (property.value.capitalize() != property.value) {
      error("Should be capitalized")
    }
  }

  fun notContains(otherName: String) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && property.value.contains(otherValue)) {
      error("Should not contain '$otherValue' ($otherName)")
    }
  }

  fun uri() {
    try {
      URL(property.value)
    } catch (urlException: MalformedURLException) {
      try {
        Paths.get(property.value)
      } catch (pathException: InvalidPathException) {
        error("Should be valid URI")
      }
    }
  }

  fun url() {
    try {
      URL(property.value)
    } catch (e: MalformedURLException) {
      error("Should be valid URL")
    }
  }

  fun path() {
    try {
      Paths.get(property.value)
    } catch (e: InvalidPathException) {
      error("Should be valid path")
    }
  }
}
