package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

open class ForkExtension(val project: Project, val props: PropsExtension) {

  @Input
  val configs = mutableListOf<Config>()

  @Internal
  val propertyDefinitions = PropertyDefinitions(this)

  fun config(name: String): Config {
    return configs.find { it.name == name }
      ?: throw ForkException("Fork configuration '$name' is yet not defined.")
  }

  fun config(configurer: Action<in SourceTargetConfig>) {
    config(SourceTargetConfig(this, Config.NAME_DEFAULT), configurer)
  }

  fun config(name: String, configurer: Action<in SourceTargetConfig>) {
    config(SourceTargetConfig(this, name), configurer)
  }

  fun inPlaceConfig(name: String, configurer: Action<in InPlaceConfig>) {
    config(InPlaceConfig(this, name), configurer)
  }

  fun properties(action: Action<in PropertyDefinitions>) {
    action.execute(propertyDefinitions)
  }

  private fun <T : Config>config(config: T, configurer: Action<in T>) {
    configs += config.apply { configurer.execute(this) }
  }

  companion object {

    const val NAME = "fork"

    fun of(project: Project): ForkExtension {
      return project.extensions.getByType(ForkExtension::class.java)
    }

  }

}
