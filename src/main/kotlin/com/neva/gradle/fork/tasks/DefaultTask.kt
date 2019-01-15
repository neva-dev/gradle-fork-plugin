package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.ConfigExtension
import org.gradle.api.tasks.Internal
import org.gradle.api.DefaultTask as BaseTask

abstract class DefaultTask : BaseTask() {

  @Internal
  protected val ext = ConfigExtension.of(project)

  init {
    group = "Fork"
    outputs.upToDateWhen { false }
  }

}
