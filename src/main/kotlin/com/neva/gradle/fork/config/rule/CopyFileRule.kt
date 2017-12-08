package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import com.neva.gradle.fork.file.filter.GitIgnoreFile
import com.neva.gradle.fork.file.visitAll
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import java.io.File

class CopyFileRule(config: Config) : AbstractRule(config) {

  companion object {
    val GIT_IGNORE_FILE = ".gitignore"

    val GIT_IGNORE_COMMENT = "#"

    val GIT_IGNORE_NEGATION = "!"
  }

  var defaultFilters = true

  var gitIgnores = true

  private val gitIgnoreFiles = mutableListOf<GitIgnoreFile>()

  private val filter = PatternSet()

  private val filteredTree: FileTree
    get() = config.sourceTree.matching(filter)

  override fun apply() {
    if (defaultFilters) {
      configureDefaultFilters()
    }
    if (gitIgnores) {
      parseGitIgnoreFiles()
    }

    copyFiles()
  }

  private fun configureDefaultFilters() {
    filter.exclude(listOf(
      "**/build",
      "**/build/*",
      "**/.gradle",
      "**/.gradle/*",
      "**/.git",
      "**/.git/*"
    ))
  }

  // TODO respect relative locations of .gitignore files
  private fun parseGitIgnoreFiles() {
    logger.info("Searching for $GIT_IGNORE_FILE file(s)")

    filteredTree.visitAll { f ->
      if (f.name == GIT_IGNORE_FILE) {
        logger.info("Respecting filters included in file: ${f.file}")
        gitIgnoreFiles += GitIgnoreFile(f.file)
      }
    }
  }

  private fun copyFiles() {
    logger.info("Copying files from ${config.sourceDir} to ${config.targetDir}")

    filteredTree.visitAll { fileDetail ->
      if (fileDetail.isDirectory) {
        return@visitAll
      }

      val source = fileDetail.file

      if (gitIgnores && gitIgnoreFiles.none { it.isValid(source) }) {
        logger.info("Skipping file ignored by Git: $source")
        return@visitAll
      }

      val target = File(config.targetDir, fileDetail.relativePath.pathString)

      FileHandler(project, source).copy(target)
    }
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
