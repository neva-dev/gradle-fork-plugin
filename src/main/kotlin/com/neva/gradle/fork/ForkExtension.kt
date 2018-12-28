package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinition
import com.neva.gradle.fork.config.properties.PropertyDefinitionDsl
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.util.ConfigureUtil

open class ForkExtension(val project: Project) {

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
    config(SourceTargetConfig(project, Config.NAME_DEFAULT), configurer)
  }

  fun config(name: String, configurer: Closure<*>) {
    config(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun config(name: String, configurer: Config.() -> Unit) {
    config(SourceTargetConfig(project, name), configurer)
  }

  fun inPlaceConfig(name: String, configurer: Closure<*>) {
    inPlaceConfig(name) { ConfigureUtil.configure(configurer, this) }
  }

  fun inPlaceConfig(name: String, configurer: Config.() -> Unit) {
    if (configExists(name)) {
      config(name).configurer()
    } else {
      config(InPlaceConfig(project, name), configurer)
    }
  }

  fun defineProperties(propertiesDefinition: Map<String, PropertyDefinitionDsl.() -> Unit>) {
    config(InPlaceConfig(project, Config.NAME_PROPERTIES)) {
      definitions += propertiesDefinition.mapValues { PropertyDefinition(it.key).apply(it.value) }
    }
  }

  private fun configExists(name: String) = configs.any { it.name == name }

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
