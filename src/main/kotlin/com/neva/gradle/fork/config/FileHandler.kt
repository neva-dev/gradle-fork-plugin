package com.neva.gradle.fork.config

import org.apache.commons.io.FileUtils
import org.gradle.util.GFileUtils
import java.io.File

class FileHandler(config : Config, val file: File) {

  val logger = config.project.logger

  fun copy(target: File) {
    GFileUtils.parentMkdirs(target)
    FileUtils.copyFile(file, target)

    logger.info("Copying file from $file to $target")
  }

  fun move(targetPath: String) {
    move(File(targetPath))
  }

  fun move(target: File) {
    if (target.exists()) {
      return
    }

    GFileUtils.parentMkdirs(target)
    if (file.isDirectory) {
      FileUtils.moveDirectory(file, target)
    } else {
      FileUtils.moveFile(file, target)
    }

    logger.info("Moving file from $file to $target")
  }

  val content: String
    get() = file.bufferedReader().use { it.readText() }

  fun amend(content: String) {
    file.printWriter().use { it.print(content) }
    logger.info("Amending file $file using new content")
  }

  fun <T> lines(block: (Sequence<String>) -> T) {
    file.bufferedReader().useLines(block)
  }

}
