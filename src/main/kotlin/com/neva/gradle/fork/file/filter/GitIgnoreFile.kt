package com.neva.gradle.fork.file.filter

import com.neva.gradle.fork.config.rule.CopyFileRule
import java.io.File

class GitIgnoreFile(val file: File) {

  private val includes = mutableListOf<Regex>()

  private val excludes = mutableListOf<Regex>()

  init {
    file.bufferedReader().useLines {
      it.forEach { line ->
        val glob = line.trim()

        if (glob.isNotBlank() && !line.startsWith(CopyFileRule.GIT_IGNORE_COMMENT)) {
          if (line.startsWith(CopyFileRule.GIT_IGNORE_NEGATION)) {
            includes += GitIgnore.globToRegex(glob.substring(1))
          } else {
            excludes += GitIgnore.globToRegex(glob)
          }
        }
      }
    }
  }

  private fun normalizedPathOf(other: File) = other.path.replace('/', '\\')

  fun isIncluded(other: File): Boolean {
    val path = normalizedPathOf(other)

    return includes.any { it.matches(path) }
  }

  fun isExcluded(other: File): Boolean {
    val path = normalizedPathOf(other)

    return excludes.any { it.matches(path) }
  }

  fun isValid(other: File): Boolean {
    return !isExcluded(other) || isIncluded(other)
  }

}
