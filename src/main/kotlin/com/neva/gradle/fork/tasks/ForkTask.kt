package com.neva.gradle.fork.tasks

import javax.inject.Inject
import com.neva.gradle.fork.config.Config

open class ForkTask @Inject constructor(config: Config) : ConfigTask(config) {

  init {
    description = "Generates new project basing on itself."
  }
}
