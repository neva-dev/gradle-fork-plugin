package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import groovy.lang.Closure
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import java.io.File

class CopyFileRule(config: Config) : AbstractRule(config) {

  companion object {
    val GIT_IGNORE_FILE_NAME = ".gitignore"

    val GIT_IGNORE_COMMENT_CHAR = "#"

    val GIT_IGNORE_NEGATION_CHAR = "!"
  }

  var gitIgnores = true

  val filter = {
    val result = PatternSet()
    result.exclude(listOf(
      "**/.gradle/*",
      "**/.git/*",
      "**/node_modules/*"
    ))
  }()

  val tree: FileTree
    get() = config.sourceTree.matching(filter)

  override fun apply() {
    if (gitIgnores) {
      configureFilterGitIgnores()
    }

    copyFiles()
  }

  // TODO support multiple nested .gitignore files
  // TODO implement gitignore like filtering (patternfilterable probably does not work in that way)
  private fun configureFilterGitIgnores() {
    val file = File(config.sourceDir, GIT_IGNORE_FILE_NAME)
    if (!file.exists()) {
      return
    }

    FileHandler(project, file).lines {
      it.forEach { line ->
        val pattern = line.trim()
        //val path = f.relativePath

        if (pattern.isNotBlank() && !line.startsWith(GIT_IGNORE_COMMENT_CHAR)) {
          if (line.startsWith(GIT_IGNORE_NEGATION_CHAR)) {
            //filter.include(pattern.substring(1))
          } else {
            filter.exclude(pattern)
          }
        }
      }
    }
  }

  private fun copyFiles() {
    tree.visit { f ->
      val source = f.file
      val target = File(config.targetDir, f.relativePath.pathString)

      if (!f.isDirectory) {
        FileHandler(project, source).copy(target)
      }
    }
  }

  fun filter(closure: Closure<*>) {
    ConfigureUtil.configure(closure, filter)
  }

}
