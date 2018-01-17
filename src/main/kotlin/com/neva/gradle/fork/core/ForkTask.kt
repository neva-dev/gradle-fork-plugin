package com.neva.gradle.fork.core

import com.neva.gradle.fork.core.config.Config
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil

open class ForkTask : DefaultTask() {

  init {
    outputs.upToDateWhen { false }
  }

  @Input
  private val configs = mutableListOf<Config>()

  @TaskAction
  fun fork() {
    if (configs.isEmpty()) {
      throw ForkException("No fork configurations defined.")
    }

    val config = if (configName.isNullOrBlank()) {
      configs.firstOrNull { it.name == Config.NAME_DEFAULT } ?: configs.first()
    } else {
      configs.firstOrNull { it.name == configName } ?: throw ForkException("Fork configuration named '$configName' not found.")
    }

    if (config.targetDir.exists()) {
      throw ForkException("Fork target directory already exists: ${config.targetDir.canonicalPath}")
    }

    config.rules.forEach { it.apply() }
  }

  val configName: String?
    get() = project.properties[CONFIG_PROP] as String?

  fun config(configurer: Closure<*>) {
    config(Config(project, CONFIG_DEFAULT), configurer)
  }

  fun config(name: String, configurer: Closure<*>) {
    config(Config(project, name), configurer)
  }

  private fun config(config: Config, configurer: Closure<*>) {
    ConfigureUtil.configure(configurer, config)
    configs += config
  }

  companion object {
    val NAME = "fork"

    val CONFIG_PROP = "fork.config"

    val CONFIG_DEFAULT = "default"
  }
}
