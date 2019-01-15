package com.neva.gradle.fork.config.properties

import java.net.MalformedURLException
import java.net.URL
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class Property(
  private val others: Map<String, Property>,
  private val definition: PropertyDefinition,
  private val prompt: PropertyPrompt
) {

  val name: String
    get() = prompt.name

  var value: String
    set(newValue) {
      prompt.value = newValue
    }
    get() = prompt.valueOrDefault ?: definition.defaultValue

  val label: String
    get() = if (required) "${prompt.label}*" else prompt.label

  val type: PropertyType = definition.type

  val options: Any? = definition.options

  val description: String = definition.description

  private val required: Boolean
    get() = definition.required

  fun other(name: String): Property {
    return others[name] ?: throw PropertyException("Property named '$name' is not defined.")
  }

  fun validate(): PropertyValidator {
    return PropertyValidator(this).apply {
      if (required && value.isBlank()) {
        error("Value is required")
      } else if (shouldBeValidated()) {
        definition.validator?.execute(this) ?: applyDefaultValidation(this)
      }
    }
  }

  fun isInvalid() = validate().hasErrors()

  private fun applyDefaultValidation(validator: PropertyValidator) = when (type) {
    PropertyType.PATH -> validatePath(validator)
    PropertyType.URL -> validateUrl(validator)
    PropertyType.URI -> validateUri(validator)
    else -> {
    }
  }

  private fun validateUri(validator: PropertyValidator) {
    try {
      URL(value)
    } catch (urlException: MalformedURLException) {
      try {
        Paths.get(value)
      } catch (pathException: InvalidPathException) {
        validator.error("Invalid URI")
      }
    }
  }

  private fun validateUrl(validator: PropertyValidator) {
    try {
      URL(value)
    } catch (e: MalformedURLException) {
      validator.error("Invalid URL")
    }
  }

  private fun validatePath(validator: PropertyValidator) {
    try {
      Paths.get(value)
    } catch (e: InvalidPathException) {
      validator.error("Invalid path")
    }
  }

  private fun shouldBeValidated() = required || value.isNotBlank()
}
