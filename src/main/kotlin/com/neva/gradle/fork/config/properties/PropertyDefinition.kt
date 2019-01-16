package com.neva.gradle.fork.config.properties

import org.gradle.api.Action
import org.gradle.internal.Actions
import javax.inject.Inject

open class PropertyDefinition @Inject constructor(val name: String) {

  init {
    if (name.isBlank()) {
      throw PropertyException("Name of property definition cannot be blank!")
    }
  }

  var type: PropertyType = determineDefaultType()

  var options: Any? = null

  var description = ""

  var defaultValue: String = ""

  var required = true

  var validator: PropertyValidator.() -> Unit = {
    if (required) {
      required()
    }
  }

  fun optional() {
    required = false
  }

  fun validator(action: Action<in PropertyValidator>) {
    validator = { Actions.with(this, action) }
  }

  fun checkbox(defaultValue: Boolean = false) {
    this.defaultValue = defaultValue.toString()
    type = PropertyType.CHECKBOX
  }

  fun select(vararg options: String) = select(options.toList())

  fun select(options: List<String>) = select(options, options.first())

  fun select(options: List<String>, defaultValue: String) {
    this.defaultValue = defaultValue
    this.options = options
    type = PropertyType.SELECT
  }

  fun password(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.PASSWORD
  }

  fun text(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.TEXT
  }

  fun path(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.PATH
    validator = { required(); path() }
  }

  fun url(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.URL
    validator = { required(); url() }
  }

  fun uri(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.URI
    validator = { required(); uri() }
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


