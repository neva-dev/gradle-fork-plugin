package com.neva.gradle.fork.config

abstract class AbstractRule(val config: Config) : Rule {

  val project = config.project

  val logger = project.logger

}
