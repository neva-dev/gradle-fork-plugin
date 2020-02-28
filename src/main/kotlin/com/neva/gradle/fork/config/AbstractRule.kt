package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.FileOperations
import com.neva.gradle.fork.file.visitAll
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import java.io.File

abstract class AbstractRule(val config: Config) : Rule {

  protected val project = config.project

  protected val logger = project.logger

  override fun validate() {
    // nothing to do
  }

  fun visitTree(
      tree: FileTree,
      condition: (FileVisitDetails) -> Boolean,
      callback: (FileHandler, FileVisitDetails) -> Unit
  ) {
    val actions = mutableListOf<() -> Unit>()
    tree.visitAll { fileDetail ->
      if (condition(fileDetail)) {
        val fileHandler = FileHandler(config, fileDetail.file)
        callback(fileHandler, fileDetail)
        actions += fileHandler.actions
      }
    }
    actions.forEach { it.invoke() }
  }

  fun visitAll(tree: FileTree, callback: (FileHandler, FileVisitDetails) -> Unit) {
    visitTree(tree, { true }, callback)
  }

  fun visitDirs(tree: FileTree, callback: (FileHandler, FileVisitDetails) -> Unit) {
    visitTree(tree, { it.isDirectory }, callback)
  }

  fun visitFiles(tree: FileTree, callback: (FileHandler, FileVisitDetails) -> Unit) {
    visitTree(tree, { !it.isDirectory }, callback)
  }

  fun removeEmptyDirs() {
    val emptyDirs = mutableListOf<File>()

    visitDirs(config.targetTree) { handler, _ ->
      val dir = handler.file
      if (FileOperations.isDirEmpty(dir)) {
        emptyDirs += dir
      }
    }

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
