package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import java.io.File

class MoveFileRule(config: Config, val searchPath: String, val replacePath: () -> String) : AbstractRule(config) {

  override fun apply() {
    visitFiles(config.targetTree, { fileDetails, actions ->
      val source = fileDetails.file
      val sourcePath = fileDetails.relativePath.pathString
      val targetPath = sourcePath.replace(searchPath, replacePath())
      val target = File(config.targetPath, targetPath)

      if (sourcePath != targetPath) {
        actions += { FileHandler(config, source).move(target) }
      }
    })
  }

}
