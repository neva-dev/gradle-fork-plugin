package com.neva.gradle.fork.config.rule

import com.neva.commons.gitignore.GitIgnore
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import com.neva.gradle.fork.file.visitAll
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import java.io.File

class CopyFileRule(config: Config) : AbstractRule(config) {

  var defaultFilters = true

  var gitIgnores = true

  private val gitIgnore by lazy { GitIgnore(File(config.sourceDir)) }

  private val filter = PatternSet()

  private val filteredTree: FileTree
    get() = config.sourceTree.matching(filter)

  override fun apply() {
    if (defaultFilters) {
      configureDefaultFilters()
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

  private fun copyFiles() {
    logger.info("Copying files from ${config.sourceDir} to ${config.targetDir}")

    filteredTree.visitAll { fileDetail ->
      if (fileDetail.isDirectory) {
        return@visitAll
      }

      val source = fileDetail.file

      if (gitIgnores && gitIgnore.isExcluded(source)) {
        logger.debug("Skipping file ignored by Git: $source")
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
