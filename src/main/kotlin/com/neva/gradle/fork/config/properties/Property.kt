package com.neva.gradle.fork.config.properties

import java.net.MalformedURLException
import java.net.URL
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class Property(private val definition: PropertyDefinition, private val prompt: PropertyPrompt) {

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

  private val required: Boolean
    get() = definition.required

  fun validate(): Validator {
    val validator = Validator(value)
    if (required && value.isBlank()) {
      validator.error("This property is required.")
      return validator
    }
    if (shouldBeValidated()) {
      when (definition.validator) {
        null -> applyDefaultValidation(validator)
        else -> definition.validator?.execute(validator)
      }
    }
    return validator
  }

  fun isInvalid() = validate().hasErrors()

  private fun applyDefaultValidation(validator: Validator) = when (type) {
    PropertyType.PATH -> validatePath(validator)
    PropertyType.URL -> validateUrl(validator)
    else -> {
    }
  }

  private fun validateUrl(validator: Validator) {
    try {
      URL(value)
    } catch (e: MalformedURLException) {
      validator.error("This URL is invalid: \"${e.message}\"")
    }
  }

  private fun validatePath(validator: Validator) {
    try {
      Paths.get(value)
    } catch (e: InvalidPathException) {
      validator.error("This path is invalid: \"${e.message}\"")
    }
  }

  private fun shouldBeValidated() = required || value.isNotBlank()
}
