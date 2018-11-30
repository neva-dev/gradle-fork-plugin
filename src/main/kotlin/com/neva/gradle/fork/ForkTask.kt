package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import groovy.lang.Closure
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOCase
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil

open class ForkTask : DefaultTask() {

  init {
    description = "Generates new project basing on itself."
  }

  @Input
  private val configs = mutableListOf<Config>()

  private val configsForked by lazy {
    val result = mutableListOf<Config>()
    configNames.forEach { configName ->
      configs.forEach { config ->
        if (FilenameUtils.wildcardMatch(config.name, configName, IOCase.INSENSITIVE)) {
          result += config
        }
      }
    }

    if (result.isEmpty()) {
      throw ForkException("Fork configuration named: $configNames not found.")
    }
    result
  }

  private val configNames: List<String>
    get() = (project.properties[CONFIG_PROP] as String?)?.split(",")
      ?: listOf(Config.NAME_DEFAULT)

  @TaskAction
  fun fork() {
    configsForked.forEach { it.validate() }
    configsForked.forEach { it.execute() }
  }

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
    config(InPlaceConfig(project, name), configurer)
  }

  private fun config(config: Config, configurer: Config.() -> Unit) {
    config.apply(configurer)
    configs += config
  }

  companion object {
    const val NAME = "fork"

    const val CONFIG_PROP = "fork.config"

    fun of(project: Project): ForkTask {
      return project.tasks.getByName(ForkTask.NAME) as ForkTask
    }
  }
}
