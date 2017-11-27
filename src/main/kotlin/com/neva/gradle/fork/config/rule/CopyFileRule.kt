package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.process.FileHandler
import org.gradle.api.tasks.util.PatternSet
import java.io.File

class CopyFileRule(config: Config) : AbstractRule(config) {

  var gitIgnores = true

  val filter = PatternSet()

  var target: File? = null

  // TODO implement relative copying and test it
  // TODO extend filter respecting gitIgnores flag
  override fun apply() {
    config.tree.matching(filter).visit { f ->
      val target = File(target, f.relativePath.pathString)

      FileHandler(project, f.file).copy(target)
    }
  }

}
