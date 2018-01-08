package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import java.io.File

class MoveFileRule(config: Config, val movements: Map<String, () -> String>) : AbstractRule(config) {

  override fun apply() {
    visitFiles(config.targetTree, { handler ->
      movements.forEach { searchPath, replacePath ->
        val targetPath = handler.filePath.replace(searchPath, replacePath())
        val target = File(config.targetPath, targetPath)

        if (handler.filePath != targetPath) {
          handler.move(target)
        }
      }
    })
  }

}
