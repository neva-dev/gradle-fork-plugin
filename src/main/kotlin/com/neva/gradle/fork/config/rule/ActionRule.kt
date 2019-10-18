package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import org.gradle.api.Action
import org.gradle.internal.Actions

class ActionRule(
    config: Config,
    private val validator: Action<in ActionRule>,
    private val executor: Action<in ActionRule>
) : AbstractRule(config) {

  override fun validate() {
    Actions.with(this, validator)
  }

  override fun execute() {
    Actions.with(this, executor)
  }

  fun fail(message: String) {
    throw ForkException(message)
  }

  override fun toString(): String {
    return "ActionRule()"
  }
}
