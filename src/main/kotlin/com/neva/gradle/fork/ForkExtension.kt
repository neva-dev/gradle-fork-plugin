package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import com.neva.gradle.fork.tasks.ConfigTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.*

open class ForkExtension(val project: Project, val props: PropsExtension) {

  private val logger = project.logger

  fun config(configurer: Action<in SourceTargetConfig>) = config(Config.NAME_DEFAULT, configurer)

  fun config(name: String, configurer: Action<in SourceTargetConfig>): TaskProvider<ConfigTask> {
    return try {
      project.tasks.named(name, ConfigTask::class.java) { configurer.execute(it.config as SourceTargetConfig) }
    } catch (e: UnknownTaskException) {
      project.tasks.register(name, ConfigTask::class.java, SourceTargetConfig(this, name)).apply {
        configure { configurer.execute(it.config as SourceTargetConfig) }
      }
    }
  }

  fun inPlaceConfig(name: String, configurer: Action<in InPlaceConfig>): TaskProvider<ConfigTask> {
    return try {
      project.tasks.named(name, ConfigTask::class.java) { configurer.execute(it.config as InPlaceConfig) }
    } catch (e: UnknownTaskException) {
      project.tasks.register(name, ConfigTask::class.java, InPlaceConfig(this, name)).apply {
        configure { configurer.execute(it.config as InPlaceConfig) }
      }
    }
  }

  @Internal
  val propertyDefinitions = PropertyDefinitions(this)

  fun properties(action: Action<in PropertyDefinitions>) {
    action.execute(propertyDefinitions)
  }

  fun loadProperties(file: File) {
    if (!file.exists()) {
      return
    }

    logger.info("Loading properties from file '$file'")

    Properties().apply {
      load(file.bufferedReader())
    }.forEach { name, value ->
      project.extensions.extraProperties.set(name.toString(), value)
    }
  }

  fun loadProperties(path: String) = loadProperties(project.file(path))

  companion object {

    const val NAME = "fork"

    fun of(project: Project): ForkExtension {
      return project.extensions.getByType(ForkExtension::class.java)
    }
  }
}
