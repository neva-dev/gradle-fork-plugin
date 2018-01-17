package com.neva.gradle.fork.core.config.rule

import com.neva.gradle.fork.core.config.AbstractRule
import com.neva.gradle.fork.core.config.Config
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

class ReplaceContentRule(config: Config, val replacements: Map<String, () -> String>) : AbstractRule(config) {

  val filter = PatternSet()

  val targetTree: FileTree
    get() = config.targetTree.matching(filter)

  override fun apply() {
    visitFiles(targetTree, { fileHandler ->
      replacements.forEach({ search, replace -> fileHandler.replace(search, replace()) })
    })
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
