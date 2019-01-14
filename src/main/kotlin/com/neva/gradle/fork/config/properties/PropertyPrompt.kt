package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.config.Config

class PropertyPrompt(
  val config: Config,
  val name: String,
  private val defaultProvider: () -> String? = { null }
) {

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

  val label: String
    get() = name
}
