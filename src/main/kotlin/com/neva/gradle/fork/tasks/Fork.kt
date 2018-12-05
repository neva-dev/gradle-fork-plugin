package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.Config
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOCase
import org.gradle.api.tasks.TaskAction

open class Fork : DefaultTask() {

  init {
    description = "Generates new project basing on itself."
  }

  private val configsForked by lazy {
    val result = mutableListOf<Config>()
    configNames.forEach { configName ->
      ext.configs.forEach { config ->
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

  companion object {
    const val NAME = "fork"

    const val CONFIG_PROP = "fork.config"
  }
}
