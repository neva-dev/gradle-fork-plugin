package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.util.ConfigureUtil

/**
 * Allows to define and execute forking configurations and also base functions.
 */
open class ConfigExtension(project: Project): BaseExtension(project) {

  @Input
  val configs = mutableListOf<Config>()

  @Internal
  val propertyDefinitions = PropertyDefinitions(this)

  fun config(name: String): Config {
    return configs.find { it.name == name }
      ?: throw ForkException("Fork configuration '$name' is yet not defined.")
  }

  fun config(configurer: Closure<*>) {
    config { ConfigureUtil.configure(configurer, this) }
  }

  fun config(configurer: Config.() -> Unit) {
    config(SourceTargetConfig(this, Config.NAME_DEFAULT), configurer)
  }

  fun config(name: String, configurer: Closure<*>) {
    config(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun config(name: String, configurer: Config.() -> Unit) {
    config(SourceTargetConfig(this, name), configurer)
  }

  fun inPlaceConfig(name: String, configurer: Closure<*>) {
    inPlaceConfig(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun inPlaceConfig(name: String, configurer: Config.() -> Unit) {
    config(InPlaceConfig(this, name), configurer)
  }

  fun properties(action: Action<in PropertyDefinitions>) {
    action.execute(propertyDefinitions)
  }

  private fun config(config: Config, configurer: Config.() -> Unit) {
    config.apply(configurer)
    configs += config
  }

  companion object {

    const val NAME = "fork"

    fun of(project: Project): ConfigExtension {
      return project.extensions.getByType(ConfigExtension::class.java)
    }

  }

}
