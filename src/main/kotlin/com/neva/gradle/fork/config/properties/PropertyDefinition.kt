package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkException

sealed class PropertyDefinitionDsl {
  var defaultValue: String? = null
  var validator: ValidatorErrorsDsl.(String) -> Unit = {}
  abstract fun required()
}

data class PropertyDefinition(val name: String, var required: Boolean = false) : PropertyDefinitionDsl() {
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
