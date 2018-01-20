package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.file.FileOperations
import java.io.File

/**
 * Firstly searches for all empty directories, then postpones deletion to be done after traversing.
 * After deleting, then parent dir might become empty so it also need to be removed.
 */
class CleanRule(config: Config) : AbstractRule(config) {

  override fun apply() {
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
        logger.info("Cleaning empty directory: $current")

        if (current.delete()) {
          current.delete()
          current = current.parentFile
        } else {
          logger.warn("Cannot delete empty directory: $current")
          break
        }
      }
    }
  }

}
