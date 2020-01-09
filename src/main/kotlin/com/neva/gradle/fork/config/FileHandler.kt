package com.neva.gradle.fork.config

import com.neva.gradle.fork.file.FileOperations
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

class FileHandler(val config: Config, val file: File) {

  private val logger = config.project.logger

  val actions = mutableListOf<() -> Unit>()

  fun copy(target: File): FileHandler {
    actions += {
      logger.info("Copying file from $file to $target")

      target.parentFile.mkdirs()
      FileUtils.copyFile(file, target)
    }

    return this
  }

  fun move(targetPath: String): FileHandler {
    return move(File(targetPath))
  }

  fun move(target: File): FileHandler {
    actions += action@{
      if (!file.exists()) {
        logger.debug("File to be moved does not exist: $file")
        return@action
      }

      if (target.exists()) {
        logger.debug("Cannot move, because target file already exists: $target")
        return@action
      }

      logger.info("Moving file from $file to $target")

      target.parentFile.mkdirs()
      FileUtils.moveFile(file, target)
    }

    return this
  }

  fun remove(): FileHandler {
    actions += action@{
      if (file.exists()) {
        logger.info("Removing file $file")

        FileUtils.deleteQuietly(file)
      }
    }

    return this
  }

  fun read(): String {
    return FileOperations.read(file)
  }

  fun write(content: String) {
    FileOperations.write(file, content)
  }

  fun amend(amender: (String) -> String): FileHandler {
    actions += {
      val source = FileOperations.read(file)
      val target = amender(source)

      if (source != target) {
        logger.info("Amended file $file")
        FileOperations.write(file, target)
      }
    }

    return this
  }

  fun replace(search: String, replace: String): FileHandler {
    val content = read()
    if (content.contains(search)) {
      if (search.contains("\n") || replace.contains("\n")) {
        if (replace.isEmpty()) {
          logger.info("Removing from file $file content:\n$search")
        } else {
          logger.info("Replacing content of file $file\nSearch:\n$search\nReplace:\n$replace")
        }
      } else {
        if (replace.isEmpty()) {
          logger.info("Removing from file $file content '$search'")
        } else {
          logger.info("Replacing '$search' with '${replace.ifBlank { "<empty>" }}' in file $file")
        }
      }

      val updatedContent = content.replace(search, replace)
      write(updatedContent)
    }

    return this
  }

  fun makeExecutable() {
    logger.info("Making file executable $file")

    Files.setPosixFilePermissions(file.toPath(), Files.getPosixFilePermissions(file.toPath()) + setOf(
      PosixFilePermission.OWNER_EXECUTE,
      PosixFilePermission.GROUP_EXECUTE
    ))
  }

  fun perform(): FileHandler {
    actions.forEach { it.invoke() }
    actions.clear()

    return this
  }

  fun expand(): FileHandler {
    val content = read()
    val updatedContent = config.renderTemplate(content)

    if (content != updatedContent) {
      logger.info("Expanding properties in file $file")

      write(updatedContent)
    }

    return this
  }

  fun render(template: String): String = config.renderTemplate(template)

  override fun toString(): String {
    return file.toString()
  }
}
