package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import groovy.lang.Closure
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import java.io.File

class CopyFileRule(config: Config) : AbstractRule(config) {

  var gitIgnores = true

  val filter = PatternSet()

  // TODO implement relative copying and test it
  // TODO extend filter respecting gitIgnores flag
  override fun apply() {
    config.sourceTree.matching(filter).visit { f ->
      val source = f.file
      val target = File(config.targetDir, f.relativePath.pathString)

      if (!f.isDirectory) {
        FileHandler(project, source).copy(target)
      }
    }
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
