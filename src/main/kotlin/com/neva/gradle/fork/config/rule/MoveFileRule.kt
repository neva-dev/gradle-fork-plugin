package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.file.FileOperations
import java.io.File

class MoveFileRule(config: Config, val movements: Map<String, () -> String>) : AbstractRule(config) {

  override fun apply() {
    moveFiles()
    removeEmptyDirs()
  }

  private fun moveFiles() {
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

  private fun removeEmptyDirs() {
    val emptyDirs = mutableListOf<File>()

    visitDirs(config.targetTree, { handler ->
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

}
