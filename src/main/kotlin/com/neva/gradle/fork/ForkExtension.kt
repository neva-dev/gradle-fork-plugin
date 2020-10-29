package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import com.neva.gradle.fork.tasks.RequirePropertiesTask
import com.neva.gradle.fork.tasks.ForkTask
import com.neva.gradle.fork.tasks.PropertiesTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.internal.Actions
import java.io.File
import java.util.*

open class ForkExtension(val project: Project, val props: PropsExtension) {

  private val logger = project.logger

  private val configs = mutableMapOf<String, Config>()

  var cached = flag("fork.cached", true)

  var interactive = flag("fork.interactive", true)

  var verbose = flag("fork.verbose", false)

  fun config(name: String = Config.NAME_FORK, configurer: Action<in SourceTargetConfig> = Actions.doNothing()): Config {
    return configs.getOrPut(name) { SourceTargetConfig(this, name) }.apply {
      configurer.execute(this as SourceTargetConfig)
    }
  }

  fun inPlaceConfig(name: String, configurer: Action<in InPlaceConfig> = Actions.doNothing()): Config {
    return configs.getOrPut(name) { InPlaceConfig(this, name) }.apply {
      configurer.execute(this as InPlaceConfig)
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

    val override = project.findProperty("fork.properties.override")?.toString()?.toBoolean() ?: false
    Properties().apply { file.inputStream().use { load(it.bufferedReader()) } }.forEach { n, v ->
      val name = n.toString()
      val value = props.encryptor.decrypt(v as String)!!

      when {
        name.startsWith(SYSTEM_PROP_PREFIX) -> System.setProperty(name.substringAfter(SYSTEM_PROP_PREFIX), value)
        else -> {
          val extraProperties = project.extensions.extraProperties
          if (override || !extraProperties.has(name)) {
            extraProperties.set(name, value)
          }
        }
      }
    }
  }

  fun loadProperties(path: String) = loadProperties(project.file(path))

  // Defining config and task at same time

  fun useForking(configName: String, options: Config.() -> Unit = {}) = config(Config.NAME_FORK).apply {
    project.tasks.register(configName, ForkTask::class.java, this)
    options()
  }

  fun useProperties(configName: String, filePath: String) = inPlaceConfig(configName).apply {
    copyTemplateFile(filePath)
    loadProperties(filePath)

    project.tasks.register(name, PropertiesTask::class.java, this)
    project.tasks.register("require${name.capitalize()}", RequirePropertiesTask::class.java, this, filePath)
  }

  private fun flag(prop: String, defaultValue: Boolean = false): Boolean {
    val value = project.properties[prop] as String? ?: return defaultValue

    return if (!value.isBlank()) value.toBoolean() else true
  }

  companion object {

    const val NAME = "fork"

    const val TASK_GROUP = "fork"

    const val SYSTEM_PROP_PREFIX = "systemProp."

    fun of(project: Project) = project.extensions.getByType(ForkExtension::class.java)
  }
}

val Project.fork get() = ForkExtension.of(this)
