package com.neva.gradle.fork.config.properties

class PropertyDefinitions {

  private val definitions = mutableMapOf<String, PropertyDefinition>()

  fun configure(configuration: Map<String, PropertyDefinition.() -> Unit>) {
    definitions += configuration.mapValues { PropertyDefinition(it.key).apply(it.value) }
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
  var validator: (Validator.() -> Unit)? = null
  var type: PropertyType = determineDefaultType()

  fun optional() {
    required = false
  }

  fun validator(validatorLogic: Validator.() -> Unit) {
    validator = validatorLogic
  }

  fun checkbox(defaultValue: Boolean = false) {
    type = PropertyType.CHECKBOX
    this.defaultValue = defaultValue.toString()
  }

  fun password(defaultValue: String = "") {
    type = PropertyType.PASSWORD
    this.defaultValue = defaultValue
  }

  fun text(defaultValue: String = "") {
    type = PropertyType.TEXT
    this.defaultValue = defaultValue
  }

  fun path(defaultValue: String = "") {
    type = PropertyType.PATH
    this.defaultValue = defaultValue
  }

  fun url(defaultValue: String = "") {
    type = PropertyType.URL
    this.defaultValue = defaultValue
  }

  private fun determineDefaultType() = when {
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
