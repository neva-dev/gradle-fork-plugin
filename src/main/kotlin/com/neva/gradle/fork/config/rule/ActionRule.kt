package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

class ActionRule(config: Config, val closure: Closure<*>) : AbstractRule(config) {

  override fun apply() {
    ConfigureUtil.configure(closure, this)
  }

  override fun toString(): String {
    return "ActionRule()"
  }

}
