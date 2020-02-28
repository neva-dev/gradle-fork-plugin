package com.neva.gradle.fork.config.properties

/**
 * Represents property found in template file or registered manually via build script.
 */
class PropertyPrompt(val name: String, private val defaultProvider: () -> String? = { null }) {

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
}
