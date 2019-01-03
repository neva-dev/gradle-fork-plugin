package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkException

class PropertiesDefinitions {
  private val definitions = mutableMapOf<String, PropertyDefinition>()

  fun configure(propertiesConfiguration: Map<String, PropertyDefinitionDsl.() -> Unit>) {
    definitions += propertiesConfiguration.mapValues { PropertyDefinition(it.key).apply(it.value) }
  }

  fun getProperty(prompt: PropertyPrompt): Property {
    val definition = definitions.getOrElse(prompt.name) {
      PropertyDefinition(prompt.name)
    }
    return Property(definition, prompt)
  }
}

sealed class PropertyDefinitionDsl {
  var defaultValue: String? = null
  var validator: ValidatorErrorsDsl.(String) -> Unit = {}
  abstract fun required()
}

class PropertyDefinition(val name: String, var required: Boolean = false) : PropertyDefinitionDsl() {
  init {
    if (name.isBlank()) throw ForkException("Name of property definition cannot be blank!")
  }

  override fun required() {
    required = true
  }

  fun validate(value: String): ValidatorErrors {
    val validatePropertyValue = validator
    val errors = ValidatorErrors()
    errors.validatePropertyValue(value)
    return errors
  }

  companion object {
    fun default(name: String) = PropertyDefinition(name)
  }
}

sealed class ValidatorErrorsDsl {
  abstract fun error(message: String)
}

class ValidatorErrors : ValidatorErrorsDsl() {
  val errors = mutableListOf<String>()

  override fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()
}
