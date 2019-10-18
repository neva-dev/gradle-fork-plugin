package com.neva.gradle.fork.config.properties

import org.apache.commons.validator.routines.UrlValidator
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class PropertyValidator(val property: Property) {

  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()

  // basic validators

  fun notEmpty() {
    if (property.value.isEmpty()) {
      error("Should not be empty")
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

  // more strict validators

  fun uri() {
    if (!checkUrl()) {
      if (!checkPath()) {
        error("Should be valid uri")
      }
    }
  }

  fun url() {
    if (!checkUrl()) {
      error("Should be valid url")
    }
  }

  fun path() {
    if (!checkPath()) {
      error("Should be valid path")
    }
  }

  // cross property validators

  fun contains(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && !property.value.contains(otherValue, ignoreCase)) {
      error("Should contain '$otherValue' ($otherName)")
    }
  }

  fun notContains(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && property.value.contains(otherValue, ignoreCase)) {
      error("Should not contain '$otherValue' ($otherName)")
    }
  }

  fun startsWith(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && !property.value.startsWith(otherValue, ignoreCase)) {
      error("Should start with '$otherValue' ($otherName)")
    }
  }

  fun notStartsWith(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && property.value.startsWith(otherValue, ignoreCase)) {
      error("Should not start with '$otherValue' ($otherName)")
    }
  }

  fun endsWith(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && !property.value.endsWith(otherValue, ignoreCase)) {
      error("Should end with '$otherValue' ($otherName)")
    }
  }

  fun notEndsWith(otherName: String, ignoreCase: Boolean = true) {
    val otherValue = property.other(otherName).value
    if (otherValue.isNotEmpty() && property.value.endsWith(otherValue, ignoreCase)) {
      error("Should not end with '$otherValue' ($otherName)")
    }
  }

  // utility methods

  fun checkRegex(regex: String, value: String = property.value) = Regex(regex).matches(value)

  fun checkTrimmingSpaces(value: String = property.value): Boolean {
    return value == value.trim()
  }

  fun checkUrl(value: String = property.value) = checkTrimmingSpaces() && URL_VALIDATOR.isValid(value)

  fun checkPath(value: String = property.value) = checkTrimmingSpaces() && try {
    Paths.get(value)
    true
  } catch (e: InvalidPathException) {
    false
  }

  companion object {
    val URL_VALIDATOR = UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES.or(UrlValidator.ALLOW_LOCAL_URLS))
  }
}
