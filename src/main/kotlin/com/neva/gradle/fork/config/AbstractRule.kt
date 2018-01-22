package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.visitAll
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails

abstract class AbstractRule(val config: Config) : Rule {

  protected val project = config.project

  protected val logger = project.logger

  fun visitTree(tree: FileTree, condition: (FileVisitDetails) -> Boolean, callback: (FileHandler, FileVisitDetails) -> Unit) {
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

}
