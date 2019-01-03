package com.neva.gradle.fork.config.properties

class PropertyPrompt(val name: String, private val defaultProvider: () -> String? = { null }) {

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

  val valueOrDefault: String?
    get() = value ?: defaultValue

  private val defaultValue: String? by lazy {
    try {
      defaultProvider()
    } catch (e: PropertyException) {
      null
    }
  }

  val type: Type
    get() = Type.of(this)

  val label: String
    get() = name
}
