package com.neva.gradle.fork.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import com.neva.gradle.fork.config.Config as Base

open class ConfigTask @Inject constructor(@Internal val config: Base) : DefaultTask() {

  init {
    description = "Executes fork configuration named '${config.name}'"
    group = "Fork"
  }

  @TaskAction
  fun evaluate() {
    config.evaluate()
  }
}
