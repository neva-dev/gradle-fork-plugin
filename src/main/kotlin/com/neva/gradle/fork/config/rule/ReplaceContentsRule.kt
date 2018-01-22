package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

class ReplaceContentsRule(config: Config, val replacements: Map<String, () -> String>) : AbstractRule(config) {

  val filter = PatternSet()

  val targetTree: FileTree
    get() = config.targetTree.matching(filter)

  override fun apply() {
    visitFiles(targetTree, { fileHandler, _ ->
      replacements.forEach({ search, replace -> fileHandler.replace(search, replace()) })
    })
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
