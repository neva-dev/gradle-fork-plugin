package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.config.Config
import javax.inject.Inject

open class PropertiesTask @Inject constructor(config: Config) : ConfigTask(config) {

  init {
    description = "Generates user-specific properties file basing on template and prompted values."
  }
}
