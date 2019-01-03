package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertiesDefinitions
import com.neva.gradle.fork.config.properties.PropertyDefinition
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.util.ConfigureUtil

open class ForkExtension(val project: Project) {

  private val propertiesDefinitions = PropertiesDefinitions()

  @Input
  val configs = mutableListOf<Config>()

  fun config(name: String): Config {
    return configs.find { it.name == name }
      ?: throw ForkException("Fork configuration '$name' is yet not defined.")
  }

  fun config(configurer: Closure<*>) {
    config { ConfigureUtil.configure(configurer, this) }
  }

  fun config(configurer: Config.() -> Unit) {
    config(SourceTargetConfig(project, propertiesDefinitions, Config.NAME_DEFAULT), configurer)
  }

  fun config(name: String, configurer: Closure<*>) {
    config(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun config(name: String, configurer: Config.() -> Unit) {
    config(SourceTargetConfig(project, propertiesDefinitions, name), configurer)
  }

  fun inPlaceConfig(name: String, configurer: Closure<*>) {
    inPlaceConfig(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun inPlaceConfig(name: String, configurer: Config.() -> Unit) {
    config(InPlaceConfig(project, propertiesDefinitions, name), configurer)
  }

  fun properties(propertiesConfiguration: Map<String, PropertyDefinition.() -> Unit>) {
    propertiesDefinitions.configure(propertiesConfiguration)
  }

  private fun config(config: Config, configurer: Config.() -> Unit) {
    config.apply(configurer)
    configs += config
  }

  companion object {

    const val NAME = "fork"

    fun of(project: Project): ForkExtension {
      return project.extensions.getByType(ForkExtension::class.java)
    }

  }

}
