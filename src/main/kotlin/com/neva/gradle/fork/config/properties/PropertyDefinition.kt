package com.neva.gradle.fork.config.properties

class PropertyDefinitions {
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

  /**
  Those values are used in DSL to simplify property type specification:
  `type = CHECKBOX`
  instead of
  `type = com.neva.gradle.fork.config.properties.PropertyType.CHECKBOX`
   */
  val PASSWORD = PropertyType.PASSWORD
  val TEXT = PropertyType.TEXT
  val CHECKBOX = PropertyType.CHECKBOX
  val PATH = PropertyType.PATH
  val URL = PropertyType.URL

  var required: Boolean = true
  var defaultValue: String = ""
  var validator: (Validator.() -> Unit)? = null
  var type: PropertyType = calculateDefaultType()

  fun optional() {
    required = false
  }

  private fun calculateDefaultType() = when {
    name.endsWith("password", true) -> PropertyType.PASSWORD
    name.startsWith("enable", true) -> PropertyType.CHECKBOX
    name.startsWith("disable", true) -> PropertyType.CHECKBOX
    name.endsWith("enabled", true) -> PropertyType.CHECKBOX
    name.endsWith("disabled", true) -> PropertyType.CHECKBOX
    name.endsWith("path", true) -> PropertyType.PATH
    name.endsWith("url", true) -> PropertyType.URL
    else -> PropertyType.TEXT
  }
}

enum class PropertyType {
  TEXT,
  PASSWORD,
  CHECKBOX,
  PATH,
  URL
}

class Validator(val value: String) {
  val errors = mutableListOf<String>()

  fun error(message: String) {
    errors.add(message)
  }

  fun hasErrors() = errors.isNotEmpty()
}
