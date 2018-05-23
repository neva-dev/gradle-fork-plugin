package com.neva.gradle.fork

import org.gradle.api.DefaultTask as BaseTask

abstract class DefaultTask : BaseTask() {

  init {
    group = "Fork"
    outputs.upToDateWhen { false }
  }

}
