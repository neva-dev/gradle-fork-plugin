package com.neva.gradle.fork.config.properties

import org.gradle.api.Action
import javax.inject.Inject

open class PropertyDefinition @Inject constructor(val name: String) {

  init {
    if (name.isBlank()) {
      throw PropertyException("Name of property definition cannot be blank!")
    }
  }

  var required: Boolean = true

  var defaultValue: String = ""

  var description = ""

  var validator: (Action<in PropertyValidator>)? = null

  var type: PropertyType = determineDefaultType()

  var options: Any? = null

  fun optional() {
    required = false
  }

  fun validator(validateAction: Action<in PropertyValidator>) {
    validator = validateAction
  }

  fun checkbox(defaultValue: Boolean = false) {
    type = PropertyType.CHECKBOX
    this.defaultValue = defaultValue.toString()
  }

  fun select(vararg options: String) = select(options.toList())

  fun select(options: List<String>) = select(options, options.first())

  fun select(options: List<String>, defaultValue: String) {
    type = PropertyType.SELECT
    this.defaultValue = defaultValue
    this.options = options
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

  fun uri(defaultValue: String = "") {
    type = PropertyType.URI
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
    name.endsWith("uri", true) -> PropertyType.URI
    else -> PropertyType.TEXT
  }
}


