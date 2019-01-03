package com.neva.gradle.fork.config.properties

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

  fun validate(propText: String): Validator {
    val validator = Validator()
    if (required || propText.isNotBlank()) {
      val validatePropertyValue = definition.validator
      validator.validatePropertyValue(propText)
    }
    if (required && propText.isBlank()) validator.error("This property is required.")
    return validator
  }
}
