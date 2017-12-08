package com.neva.gradle.fork.file

import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails

/**
 * @link https://issues.gradle.org/browse/GRADLE-1883
 * @link https://github.com/gradle/gradle/issues/1348
 */
fun FileTree.visitAll(visitor: (FileVisitDetails) -> Unit): FileTree {
  var reset = false
  val current = DirectoryScanner.getDefaultExcludes()

  current.forEach { DirectoryScanner.removeDefaultExclude(it) }
  return visit({ f ->
    if (!reset) {
      current.forEach { DirectoryScanner.addDefaultExclude(it) }
      reset = true
    }
    visitor(f)
  })
}
