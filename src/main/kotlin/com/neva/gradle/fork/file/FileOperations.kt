package com.neva.gradle.fork.file

import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileOperations {

  fun isDirEmpty(dir: File): Boolean {
    return dir.exists() && isDirEmpty(Paths.get(dir.absolutePath))
  }

  fun isDirEmpty(dir: Path): Boolean {
    Files.newDirectoryStream(dir).use { dirStream -> return !dirStream.iterator().hasNext() }
  }

  fun read(file: File): String {
    return file.inputStream().bufferedReader().use { it.readText() }
  }

  fun write(file: File, content: String) {
    file.printWriter().use { it.print(content) }
  }

  fun amend(file: File, amender: (String) -> String) {
    val source = read(file)
    val target = amender(source)
    write(file, target)
  }
}

/**
 * @link https://issues.gradle.org/browse/GRADLE-1883
 * @link https://github.com/gradle/gradle/issues/1348
 */
fun FileTree.visitAll(visitor: (FileVisitDetails) -> Unit): FileTree {
  var reset = false
  val current = DirectoryScanner.getDefaultExcludes()

  current.forEach { DirectoryScanner.removeDefaultExclude(it) }
  return visit { f ->
    if (!reset) {
      current.forEach { DirectoryScanner.addDefaultExclude(it) }
      reset = true
    }
    visitor(f)
  }
}
