package com.neva.gradle.fork.config

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import java.io.File

class FileHandler(project: Project, val file: File) {

  val logger = project.logger

  fun copy(target: File) {
    GFileUtils.parentMkdirs(target)

    FileUtils.copyFile(file, target)

    logger.info("Copying file from $file to $target")
  }

  fun move(target: File) {
    GFileUtils.parentMkdirs(target)

    FileUtils.moveFile(file, target)

    logger.info("Moving file from $file to $target")
  }

  val content: String
    get() = file.bufferedReader().use { it.readText() }

  fun amend(content: String) {
    file.printWriter().use { it.print(content) }
    logger.info("Amending file $file using new content")
  }

}
