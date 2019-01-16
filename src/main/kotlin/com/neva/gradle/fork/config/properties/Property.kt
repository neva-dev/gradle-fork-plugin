package com.neva.gradle.fork.config.properties

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
    get() = if (definition.required) {
      "${prompt.label}*"
    } else {
      prompt.label
    }

  val type: PropertyType = definition.type

  val options: Any? = definition.options

  val description: String = definition.description

  val invalid: Boolean
    get() = validate().hasErrors()

  fun validate(): PropertyValidator = PropertyValidator(this).apply(definition.validator)

  fun other(name: String): Property {
    return others[name] ?: throw PropertyException("Property named '$name' is not defined.")
  }
}
