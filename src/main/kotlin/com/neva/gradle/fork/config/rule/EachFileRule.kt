package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import org.gradle.api.Action
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet

class EachFileRule(config: Config, val action: Action<in FileHandler>) : AbstractRule(config) {

  val filter = PatternSet()

  val targetTree: FileTree
    get() = config.targetTree.matching(filter)

  override fun execute() {
    visitFiles(targetTree) { fileHandler, _ ->
      action.execute(fileHandler)
    }
  }

  fun filter(options: Action<in PatternSet>) {
    options.execute(filter)
  }

  override fun toString(): String {
    return "EachFileRule()"
  }

}
