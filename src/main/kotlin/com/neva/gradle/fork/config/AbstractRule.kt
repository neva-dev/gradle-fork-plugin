package com.neva.gradle.fork.config

import org.gradle.api.file.FileVisitDetails
import java.io.File

abstract class AbstractRule(val config: Config) : Rule {

  val project = config.project

  val logger = project.logger

  fun toTargetFile(fileDetail : FileVisitDetails): File {
    return File(config.targetDir, fileDetail.relativePath.pathString)
  }

}
