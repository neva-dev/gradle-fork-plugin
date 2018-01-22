package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.FileOperations
import org.apache.commons.io.FileUtils
import org.gradle.util.GFileUtils
import java.io.File

class FileHandler(val config: Config, val file: File) {

  private val logger = config.project.logger

  val actions = mutableListOf<() -> Unit>()

  fun copy(target: File) {
    actions += {
      logger.info("Copying file from $file to $target")

      GFileUtils.parentMkdirs(target)
      FileUtils.copyFile(file, target)
    }
  }

  fun move(targetPath: String) {
    move(File(targetPath))
  }

  fun move(target: File) {
    if (target.exists()) {
      return
    }

    actions += {
      logger.info("Moving file from $file to $target")

      GFileUtils.parentMkdirs(target)
      FileUtils.moveFile(file, target)
    }
  }

  fun read(): String {
    return FileOperations.read(file)
  }

  fun write(content: String) {
    FileOperations.write(file, content)
  }

  fun amend(amender: (String) -> String) {
    actions += {
      logger.info("Amending file $file")
      FileOperations.amend(file, amender)
    }
  }

  fun replace(search: String, replace: String) {
    val content = read()
    if (content.contains(search)) {
      logger.info("Replacing '$search' with '$replace' in file $file")

      val updatedContent = content.replace(search, replace)
      write(updatedContent)
    }
  }

  fun invoke() {
    actions.forEach { it.invoke() }
    actions.clear()
  }

  fun expand() {
    val content = read()
    val updatedContent = config.renderTemplate(content)

    if (content != updatedContent) {
      logger.info("Expanding properties in file $file")

      write(updatedContent)
    }
  }

  override fun toString(): String {
    return file.toString()
  }
}
