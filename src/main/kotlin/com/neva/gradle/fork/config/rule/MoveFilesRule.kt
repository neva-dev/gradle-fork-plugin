package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.file.FileOperations
import java.io.File

class MoveFilesRule(config: Config, movements: Map<String, () -> String>) : AbstractRule(config) {

  private val movements by lazy { movements.mapValues { it.value() } }

  override fun apply() {
    moveFiles()
    removeEmptyDirs()
  }

  private fun moveFiles() {
    visitFiles(config.targetTree, { handler, details ->
      movements.forEach { searchPath, replacePath ->
        val sourcePath = details.relativePath.pathString
        val targetPath = sourcePath.replace(searchPath, replacePath)
        val target = File(config.targetPath, targetPath)

        if (sourcePath != targetPath) {
          handler.move(target)
        }
      }
    })
  }

  private fun removeEmptyDirs() {
    val emptyDirs = mutableListOf<File>()

    visitDirs(config.targetTree, { handler, _ ->
      val dir = handler.file
      if (FileOperations.isDirEmpty(dir)) {
        emptyDirs += dir
      }
    })

    emptyDirs.forEach { dir ->
      var current = dir
      while (current != config.targetDir && FileOperations.isDirEmpty(current)) {
        logger.debug("Cleaning empty directory: $current")

        if (current.delete()) {
          current.delete()
          current = current.parentFile
        } else {
          logger.debug("Cannot delete empty directory: $current")
          break
        }
      }
    }
  }

  override fun toString(): String {
    return "MoveFilesRule(movements=$movements)"
  }

}
