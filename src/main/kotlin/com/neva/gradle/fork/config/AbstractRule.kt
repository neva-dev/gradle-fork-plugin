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

  fun visitTree(tree: FileTree, callback: (FileVisitDetails) -> Unit) {
    tree.visitAll { fileDetail ->
      if (!fileDetail.isDirectory) {
        callback(fileDetail)
      }
    }
  }

}
