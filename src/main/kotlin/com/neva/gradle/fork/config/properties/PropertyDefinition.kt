package com.neva.gradle.fork.config.properties

class PropertiesDefinitions {
  private val definitions = mutableMapOf<String, PropertyDefinition>()

  fun configure(propertiesConfiguration: Map<String, PropertyDefinition.() -> Unit>) {
    definitions += propertiesConfiguration.mapValues { PropertyDefinition(it.key).apply(it.value) }
  }

  fun getProperty(prompt: PropertyPrompt): Property {
    val definition = definitions.getOrElse(prompt.name) {
      PropertyDefinition(prompt.name)
    }
    return Property(definition, prompt)
  }
}

class PropertyDefinition(val name: String) {
  init {
    if (name.isBlank()) throw PropertyException("Name of property definition cannot be blank!")
  }

  var required: Boolean = true
  var defaultValue: String = ""
  var validator: Validator.(String) -> Unit = {}

  fun optional() {
    required = false
  }
}

class Validator {
  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()
}
