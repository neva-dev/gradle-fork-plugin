package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import com.neva.gradle.fork.tasks.ConfigTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.internal.Actions
import java.io.File
import java.util.*

open class ForkExtension(val project: Project, val props: PropsExtension) {

  private val logger = project.logger

  private val configs = mutableMapOf<String, Config>()


  fun config(name: String = Config.NAME_DEFAULT, configurer: Action<in SourceTargetConfig> = Actions.doNothing()): Config {
    return configs.getOrPut(name) { SourceTargetConfig(this, name) }.apply { configurer.execute(this as SourceTargetConfig) }
  }

  fun inPlaceConfig(name: String, configurer: Action<in InPlaceConfig> = Actions.doNothing()) = configs.getOrPut(name) { InPlaceConfig(this, name) }.apply { configurer.execute(this as InPlaceConfig) }

  fun props(configurer: Action<in InPlaceConfig> = Actions.doNothing()) = inPlaceConfig(Config.NAME_PROPERTIES, configurer)

  @Internal
  val propertyDefinitions = PropertyDefinitions(this)

  fun properties(action: Action<in PropertyDefinitions>) {
    action.execute(propertyDefinitions)
  }

  fun properties(
    configName: String,
    filePath: String,
    generateOptions: Action<in Task> = Actions.doNothing(),
    requireOptions: Action<in Task> = Actions.doNothing()
  ) {
    props(Action { c ->
      c.copyTemplateFile(filePath)

      project.tasks.register(configName, ConfigTask::class.java, c as Config).configure(generateOptions)
      project.tasks.register("require${configName.capitalize()}") {
        it.doLast {
          if (!c.getTargetFile(filePath).exists()) {
            throw ForkException("Required properties file '$filePath' does not exist!\n" +
              "Run task '$configName' to generate it interactively.")
          }
        }
      }.configure(requireOptions)
    })

    loadProperties(filePath)
  }

  fun loadProperties(file: File) {
    if (!file.exists()) {
      return
    }

    logger.info("Loading properties from file '$file'")

    Properties().apply { load(file.bufferedReader()) }.forEach { n, v ->
      val name = n.toString()
      val value = props.encryptor.decrypt(v as String)!!

      when {
        name.startsWith(SYSTEM_PROP_PREFIX) -> System.setProperty(name.substringAfter(SYSTEM_PROP_PREFIX), value)
        else -> project.extensions.extraProperties.set(name, value)
      }
    }
  }

  fun loadProperties(path: String) = loadProperties(project.file(path))

  companion object {

    const val NAME = "fork"

    const val SYSTEM_PROP_PREFIX = "systemProp."

    fun of(project: Project): ForkExtension {
      return project.extensions.getByType(ForkExtension::class.java)
    }
  }
}
