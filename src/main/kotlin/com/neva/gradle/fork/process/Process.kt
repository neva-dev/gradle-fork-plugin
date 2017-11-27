package com.neva.gradle.fork.process

import com.neva.gradle.fork.config.Config
import javax.inject.Inject

class Process @Inject constructor(
  private val config: Config
) : Runnable {

  private val logger = config.project.logger

  override fun run() {
    logger.info("Forking using config $config")
    config.rules.forEach { it.apply() }
  }

}
