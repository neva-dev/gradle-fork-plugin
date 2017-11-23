package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File

class Config(val root: File) {

  val contentRules = mutableListOf<FileRule>()

  val pathRules = mutableListOf<FileRule>()

  var gitIgnores = true

  fun scan(): List<FileHandler> {
    if (!root.exists()) {
      throw ForkException("Config root does not exist: $root")
    }

    var files = FileUtils.listFilesAndDirs(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
    if (gitIgnores) {
      files = filterGitIgnores(files)
    }

    return files.map { FileHandler(it) }
  }

  private fun filterGitIgnores(files: Collection<File>): List<File> {
    val ignoreFiles = listOf<File>() // TODO read .gitignore files under root and calculate file filter

    return files.filter { true }
  }

  override fun toString(): String {
    return "Config(root='$root', gitIgnores=$gitIgnores)"
  }

}
