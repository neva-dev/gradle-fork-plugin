package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.properties.PropertyDefinition
import com.neva.gradle.fork.config.properties.ValidatorErrors

class Property(private val definition: PropertyDefinition, private val prompt: PropertyPrompt) {

  val name: String
    get() = prompt.name

  val valueOrDefault: String
    get() = definition.defaultValue ?: prompt.valueOrDefault

  val label: String
    get() = if (required) "${prompt.label}*" else prompt.label

  val type: PropertyPrompt.Type
    get() = prompt.type

  private val required: Boolean
    get() = prompt.required || definition.required

  fun validate(propText: String): ValidatorErrors {
    val result = definition.validate(propText)
    if (required && propText.isBlank()) result.error("This property is required.")
    return result
  }
}
