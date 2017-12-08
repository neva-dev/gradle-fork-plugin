package com.neva.gradle.fork

import com.neva.gradle.fork.file.filter.GitIgnore
import org.junit.Test
import org.junit.Assert.*

class GitIgnoreTest {

  @Test
  fun shouldConvertGlobToPattern() {
    val pattern = GitIgnore.globToPattern("**/*.xml")

    assertTrue(pattern.matcher("dir/xx.xml").matches())
    assertTrue(pattern.matcher("dir/xx.xml/aaa.csv").matches())
    assertTrue(pattern.matcher("xx.xml").matches())
    assertFalse(pattern.matcher("xx.csv").matches())
  }

  @Test
  fun shouldConvertGlobToRegex() {
    val regex = GitIgnore.globToRegex("**/*.xml")

    assertTrue(regex.matches("dir/xx.xml"))
    assertTrue(regex.matches("xx.xml"))
    assertFalse(regex.matches("xx.csv"))


    assertTrue(GitIgnore.globToRegex("node_modules").matches("abc/node_modules/sth.js"))
    assertTrue(GitIgnore.globToRegex("node_modules/x").matches("node_modules/x/y"))
  }

}
