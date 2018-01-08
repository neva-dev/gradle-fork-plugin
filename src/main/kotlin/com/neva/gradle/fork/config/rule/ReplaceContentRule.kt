package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

class ReplaceContentRule(config: Config, val replacements: Map<String, () -> String>) : AbstractRule(config) {

  val filter = PatternSet()

  private val targetTree: FileTree
    get() = config.targetTree.matching(filter)

  override fun apply() {
    visitFiles(targetTree, { fileDetails, actions ->
      actions += {
        val fileHandler = FileHandler(config, fileDetails.file)
        replacements.forEach({ search, replace -> fileHandler.replace(search, replace()) })
      }
    })
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
