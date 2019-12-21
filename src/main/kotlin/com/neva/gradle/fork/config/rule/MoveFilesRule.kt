package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import java.io.File

class MoveFilesRule(config: Config, movements: Map<String, () -> String>) : AbstractRule(config) {

  private val movements by lazy { movements.mapValues { it.value() } }

  override fun execute() {
    moveFiles()
    removeEmptyDirs()
  }

  private fun moveFiles() {
    visitFiles(config.targetTree) { handler, details ->
      movements.forEach { (searchPath, replacePath) ->
        val sourcePath = details.relativePath.pathString
        val targetPath = sourcePath.replace(searchPath, replacePath)
        val target = File(config.targetPath, targetPath)

        if (sourcePath != targetPath) {
          handler.move(target)
        }
      }
    }
  }

  override fun toString(): String {
    return "MoveFilesRule(movements=$movements)"
  }
}
