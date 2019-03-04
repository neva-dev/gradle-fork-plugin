package com.neva.gradle.fork.config.properties

/**
 * Represents prompted property bound with its definition.
 * Can interact with other properties using common context.
 */
class Property(
  val definition: PropertyDefinition,
  private val prompt: PropertyPrompt,
  private val context: PropertyContext
) {

  val name: String
    get() = prompt.name

  var value: String
    set(newValue) {
      prompt.value = newValue
    }
    get() = prompt.valueOrDefault ?: definition.defaultValue

  val label: String
    get() {
      val text = definition.label ?: prompt.label

      return if (definition.required) {
        "$text*"
      } else {
        text
      }
    }

  val type: PropertyType = definition.type

  val options: Any? = definition.options

  val description: String = definition.description

  val invalid: Boolean
    get() = validate().hasErrors()

  var enabled: Boolean = true

  var required: Boolean = definition.required

  fun control() = definition.controller(this)

  fun validate(): PropertyValidator {
    return PropertyValidator(this).apply {
      if (enabled && (required || (!required && value.isNotEmpty()))) {
        definition.validator(this)
      }
    }
  }

  fun toggle() {
    enabled = !enabled
  }

  fun toggle(vararg names: String) = toggle(names.toList())

  fun toggle(names: List<String>) = toggle(value.toBoolean(), names)

  fun toggle(flag: Boolean, vararg names: String) = toggle(flag, names.toList())

  fun toggle(flag: Boolean, patterns: List<String>) {
    for (pattern in patterns) {
      val others = others(pattern)
      for (property in others) {
        property.enabled = flag
      }
    }
  }

  fun other(name: String) = context.get(name)

  fun others(pattern: String) = context.find(pattern)

  override fun toString(): String {
    return "Property(name=$name, type=$type, value=$value)"
  }
}
