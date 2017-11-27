package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.rule.CopyFileRule
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.util.ConfigureUtil

class Config(val project: Project, val tree: ConfigurableFileTree) {

  val root = tree.dir

  val rules = mutableListOf<Rule>()

  val ruleCount : Int
    get() = rules.size

  override fun toString(): String {
    return "Config(root='$root',ruleCount=$ruleCount"
  }

  fun copyFile(closure: Closure<*>) {
    val rule = CopyFileRule( this)
    ConfigureUtil.configure(closure, rule)
    rules += rule
  }

  // TODO respect template variables in 'replacePath' arg and prompt for them if unspecified
  fun moveFile(searchPath: String, replacePath: String) {

  }

  // TODO respect template variables in 'replace' arg and prompt for them if unspecified
  fun amendContent(search: String, replace: String) {

  }

}
