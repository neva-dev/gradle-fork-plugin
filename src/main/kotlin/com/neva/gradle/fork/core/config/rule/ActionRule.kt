package com.neva.gradle.fork.core.config.rule

import com.neva.gradle.fork.core.config.AbstractRule
import com.neva.gradle.fork.core.config.Config
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

class ActionRule(config: Config, val closure: Closure<*>) : AbstractRule(config) {

  override fun apply() {
    ConfigureUtil.configure(closure, this)
  }

}
