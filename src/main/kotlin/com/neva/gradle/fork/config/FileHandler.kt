package com.neva.gradle.fork.config

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.util.GFileUtils
import java.io.File

class FileHandler(val origin: File) {

  var file = origin

  val changes = mutableListOf<String>()

  fun move(target: File) {
    GFileUtils.parentMkdirs(target)

    FileUtils.moveFile(file, target)

    file = target
    changes += "Moving file from $file to $target"
  }

  val content: String
    get() = file.bufferedReader().use { it.readText() }

  fun amend(content: String) {
    file.printWriter().use { it.print(content) }
    changes += "Amending file $file using new content"
  }

}
