package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

class ActionRule(
  config: Config,
  private val validator: Closure<*>,
  private val executor: Closure<*>
) : AbstractRule(config) {

  override fun validate() {
    ConfigureUtil.configure(validator, this)
  }

  override fun execute() {
    ConfigureUtil.configure(executor, this)
  }

  fun fail(message: String) {
    throw ForkException(message)
  }

  override fun toString(): String {
    return "ActionRule()"
  }

}
