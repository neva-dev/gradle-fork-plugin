package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.visitAll
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import java.io.File

abstract class AbstractRule(val config: Config) : Rule {

  val project = config.project

  val logger = project.logger

  fun toTargetFile(fileDetail: FileVisitDetails): File {
    return File(config.targetDir, fileDetail.relativePath.pathString)
  }

  fun visitTree(tree: FileTree, condition: (FileVisitDetails) -> Boolean, callback: (FileVisitDetails, MutableList<() -> Unit>) -> Unit) {
    val actions = mutableListOf<() -> Unit>()
    tree.visitAll { fileDetail ->
      if (condition(fileDetail)) {
        callback(fileDetail, actions)
      }
    }
    actions.forEach { it.invoke() }
  }

  fun visitDirs(tree: FileTree, callback: (FileVisitDetails, MutableList<() -> Unit>) -> Unit) {
    visitTree(tree, { it.isDirectory }, callback)
  }

  fun visitFiles(tree: FileTree, callback: (FileVisitDetails, MutableList<() -> Unit>) -> Unit) {
    visitTree(tree, { !it.isDirectory }, callback)
  }

}
