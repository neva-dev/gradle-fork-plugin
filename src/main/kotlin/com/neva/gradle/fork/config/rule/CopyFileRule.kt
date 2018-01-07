package com.neva.gradle.fork.config.rule

import com.neva.commons.gitignore.GitIgnore
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

class CopyFileRule(config: Config) : AbstractRule(config) {

  var defaultFilters = true

  var gitIgnores = true

  private val gitIgnore by lazy { GitIgnore(config.sourceDir) }

  private val filter = PatternSet()

  private val sourceTree: FileTree
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

    visitFiles(sourceTree, { fileDetail, _ ->
      val source = fileDetail.file

      if (gitIgnores && gitIgnore.isExcluded(source)) {
        logger.debug("Skipping file ignored by Git: $source")
        return@visitFiles
      }

      val target = toTargetFile(fileDetail)

      FileHandler(config, source).copy(target)
    })
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
