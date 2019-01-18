package com.neva.gradle.fork.config.properties

class Property(
  val context: PropertyContext,
  val definition: PropertyDefinition,
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

  var enabled: Boolean = true

  fun control() = definition.controller(this)

  fun validate(): PropertyValidator = if (enabled) {
    PropertyValidator(this).apply(definition.validator)
  } else {
    PropertyValidator(this)
  }

  fun toggle() {
    enabled = !enabled
  }

  fun toggle(vararg otherNames: String) = toggle(otherNames.toList())

  fun toggle(otherNames: List<String>) = toggle(value.toBoolean(), otherNames)

  fun toggle(flag: Boolean, vararg names: String) = toggle(flag, names.toList())

  fun toggle(flag: Boolean, otherNames: List<String>) {
    otherNames.forEach { pattern -> context.find(pattern).forEach { it.enabled = flag } }
  }
}
