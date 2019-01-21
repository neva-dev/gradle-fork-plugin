package com.neva.gradle.fork.config.properties

import org.gradle.api.Action
import org.gradle.internal.Actions
import javax.inject.Inject

/**
 * Represents rich property definition (adds extra information to pure prompt like type, description, validator etc).
 */
open class PropertyDefinition @Inject constructor(val name: String) {

  var type: PropertyType = PropertyType.TEXT

  var options: Any? = null

  var description = ""

  var defaultValue: String = ""

  var required = true

  var controller: Property.() -> Unit = {}

  var validator: PropertyValidator.() -> Unit = { notBlank() }

  init {
    if (name.isBlank()) {
      throw PropertyException("Name of property definition cannot be blank!")
    }

    when {
      name.endsWith("password", true) -> password()
      name.startsWith("enable", true) -> checkbox()
      name.startsWith("disable", true) -> checkbox()
      name.endsWith("enabled", true) -> checkbox()
      name.endsWith("disabled", true) -> checkbox()
      name.endsWith("path", true) -> path()
      name.endsWith("url", true) -> url()
      name.endsWith("uri", true) -> uri()
      else -> text()
    }
  }

  fun optional() {
    required = false
  }

  fun controller(action: Action<in Property>) {
    controller = { Actions.with(this, action) }
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
    validator = { notBlank(); path() }
  }

  fun url(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.URL
    validator = { notBlank(); url() }
  }

  fun uri(defaultValue: String = "") {
    this.defaultValue = defaultValue
    type = PropertyType.URI
    validator = { notBlank(); uri() }
  }
}


