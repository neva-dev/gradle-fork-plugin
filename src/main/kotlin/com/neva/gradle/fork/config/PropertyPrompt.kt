package com.neva.gradle.fork.config

import org.apache.commons.lang3.StringUtils

class PropertyPrompt(val name: String, private val defaultProvider: () -> String?) {

  enum class Type(val check: (PropertyPrompt) -> Boolean) {
    PASSWORD({ it.name.endsWith("password", true) }),
    TEXT({ true });

    companion object {
      fun of(prompt: PropertyPrompt): Type {
        return values().first { it.check(prompt) }
      }
    }
  }

  var value: String? = null

  val valueOrDefault: String
    get() = value ?: defaultValue ?: ""

  val required: Boolean
    get() = defaultValue == null

  val valid: Boolean
    get() = !required || valueOrDefault.isNotEmpty()

  val defaultValue: String? by lazy {
    try {
      defaultProvider()
    } catch (e: PropertyException) {
      null
    }
  }

  val type: Type
    get() = Type.of(this)

  val label: String
    get() = StringUtils.capitalize(name.replace(String.format("%s|%s|%s",
      "(?<=[A-Z])(?=[A-Z][a-z])",
      "(?<=[^A-Z])(?=[A-Z])",
      "(?<=[A-Za-z])(?=[^A-Za-z])"
    ).toRegex(), " "))
}
