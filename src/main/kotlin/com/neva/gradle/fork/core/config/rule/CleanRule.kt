package com.neva.gradle.fork.core.config.rule

import com.neva.gradle.fork.core.config.AbstractRule
import com.neva.gradle.fork.core.config.Config
import com.neva.gradle.fork.core.file.FileOperations

class CleanRule(config: Config) : AbstractRule(config) {

  override fun apply() {
    visitDirs(config.targetTree, { handler ->
      var dir = handler.file

      while (dir != config.targetDir && FileOperations.isDirEmpty(dir)) {
        dir.delete()
        dir = dir.parentFile
      }
    })
  }

}
