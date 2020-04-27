package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.ForkExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import com.neva.gradle.fork.config.Config

open class ConfigTask @Inject constructor(private val config: Config) : DefaultTask() {

  init {
    description = "Executes fork configuration named '${config.name}'"
    group = ForkExtension.TASK_GROUP
  }

  @TaskAction
  fun evaluate() {
    config.evaluate()
  }
}
