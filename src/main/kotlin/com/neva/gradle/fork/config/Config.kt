package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.rule.AmendContentRule
import com.neva.gradle.fork.config.rule.CopyFileRule
import com.neva.gradle.fork.config.rule.MoveFileRule
import groovy.lang.Closure
import org.apache.commons.lang3.text.StrSubstitutor
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.util.ConfigureUtil
import java.io.File
import java.util.regex.Pattern

class Config(val project: Project, val name: String) {

  companion object {
    val NAME_DEFAULT = "default"
  }

  val prompts = mutableMapOf<String, () -> String>()

  val props by lazy { promptFill() }

  val rules = mutableListOf<Rule>()

  val sourceDir: String by lazy(promptProp("fork.sourceDir", {
    project.projectDir.absolutePath
  }))

  val sourceTree: FileTree by lazy { project.fileTree(sourceDir) }

  val targetDir: String by lazy(promptProp("fork.targetDir", {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  }))

  val targetTree: FileTree by lazy { project.fileTree(targetDir) }

  fun promptProp(prop: String): () -> String {
    return promptProp(prop, {
      throw ForkException("Fork property named '$prop' has no value provided.")
    })
  }

  fun promptProp(prop: String, defaultProvider: () -> String): () -> String {
    prompts[prop] = defaultProvider

    return { prompts[prop]!!() }
  }

  fun promptTemplate(template: String): () -> String {
    parseTemplate(template).forEach { prop ->
      prompts[prop] = {
        throw ForkException("Fork template property '$prop' has no value provided.")
      }
    }

    return { renderTemplate(template) }
  }

  private fun parseTemplate(template: String): List<String> {
    val p = Pattern.compile("\\{\\{(.+?)\\}\\}")
    val m = p.matcher(template)

    val result = mutableListOf<String>()

    var i = 1
    while (m.find()) {
      result += m.group(i)
      i++
    }

    return result
  }

  private fun renderTemplate(template: String): String {
    return StrSubstitutor(props, "{{", "}}").replace(template)
  }

  private fun promptFill(): Map<String, String> {
    return prompts.mapValues({ (prop, defaultValue) ->
      project.properties.getOrDefault(prop, defaultValue) as String
    })
  }

  private fun rule(rule: Rule, configurer: Closure<*>) {
    ConfigureUtil.configure(configurer, rule)
    rules += rule
  }

  private fun rule(rule: Rule) {
    rules += rule
  }

  fun copyFile(configurer: Closure<*>) {
    rule(CopyFileRule(this), configurer)
  }

  fun moveFile(searchPath: String, replacePath: String) {
    rule(MoveFileRule(this, searchPath, promptTemplate(replacePath)))
  }

  fun amendContent(search: String, replace: String) {
    rule(AmendContentRule(this, search, promptTemplate(replace)))
  }

  override fun toString(): String {
    return "Config(name=$name,ruleCount=${rules.size})"
  }

}
