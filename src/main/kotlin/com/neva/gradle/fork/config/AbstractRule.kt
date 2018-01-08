package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.visitAll
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails

abstract class AbstractRule(val config: Config) : Rule {

  val project = config.project

  val logger = project.logger

  fun visitTree(tree: FileTree, condition: (FileVisitDetails) -> Boolean, callback: (FileHandler) -> Unit) {
    val actions = mutableListOf<() -> Unit>()
    tree.visitAll { fileDetail ->
      if (condition(fileDetail)) {
        val fileHandler = FileHandler(config, fileDetail)
        callback(fileHandler)
        actions += fileHandler.actions
      }
    }
    actions.forEach { it.invoke() }
  }

  fun visitAll(tree: FileTree, callback: (FileHandler) -> Unit) {
    visitTree(tree, { true }, callback)
  }

  fun visitDirs(tree: FileTree, callback: (FileHandler) -> Unit) {
    visitTree(tree, { it.isDirectory }, callback)
  }

  fun visitFiles(tree: FileTree, callback: (FileHandler) -> Unit) {
    visitTree(tree, { !it.isDirectory }, callback)
  }

}
