package com.neva.gradle.fork.config

import org.apache.commons.io.FileUtils
import org.gradle.util.GFileUtils
import java.io.File

class FileHandler(config: Config, val file: File) {

  val logger = config.project.logger

  fun copy(target: File) {
    logger.info("Copying file from $file to $target")

    GFileUtils.parentMkdirs(target)
    FileUtils.copyFile(file, target)
  }

  fun move(targetPath: String) {
    move(File(targetPath))
  }

  fun move(target: File) {
    if (target.exists()) {
      return
    }

    logger.info("Moving file from $file to $target")

    GFileUtils.parentMkdirs(target)
    FileUtils.moveFile(file, target)
  }

  private fun read(): String {
    return file.inputStream().bufferedReader().use { it.readText() }
  }

  private fun write(content: String) {
    file.printWriter().use { it.print(content) }
  }

  fun replace(search: String, replace: String) {
    val content = read()
    if (content.contains(search)) {
      logger.info("Replacing '$search' with '$replace' in file $file")

      val updatedContent = content.replace(search, replace)
      write(updatedContent)
    }
  }

}
