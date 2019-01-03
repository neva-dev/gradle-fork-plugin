package com.neva.gradle.fork.config.properties

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
    val validator = Validator()
    if (required || value.isNotBlank()) {
      val validatePropertyValue = definition.validator
      validator.validatePropertyValue(value)
    }
    if (required && value.isBlank()) validator.error("This property is required.")
    return validator
  }
}
