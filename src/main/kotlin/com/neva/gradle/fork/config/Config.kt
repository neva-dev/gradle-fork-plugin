package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.rule.CleanRule
import com.neva.gradle.fork.config.rule.CopyFileRule
import com.neva.gradle.fork.config.rule.MoveFileRule
import com.neva.gradle.fork.config.rule.ReplaceContentRule
import groovy.lang.Closure
import org.apache.commons.lang3.text.StrSubstitutor
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.util.ConfigureUtil
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.regex.Pattern

class Config(val project: Project, val name: String) {

  companion object {
    val NAME_DEFAULT = "default"
  }

  val prompts = mutableMapOf<String, () -> String>()

  val props by lazy { promptFill() }

  val rules = mutableListOf<Rule>()

  val sourcePath: String by lazy(promptProp("sourcePath", {
    project.projectDir.absolutePath
  }))

  val sourceDir: File
    get() = File(sourcePath)

  val sourceTree: FileTree
    get() = project.fileTree(sourcePath)

  val targetPath: String by lazy(promptProp("targetPath", {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  }))

  val targetDir: File
    get() = File(targetPath)

  val targetTree: FileTree
    get() = project.fileTree(targetPath)

  var textFiles = mutableListOf(
    "**/*.gradle", "**/*.xml", "**/*.properties", "**/*.js", "**/*.json", "**/*.css", "**/*.scss",
    "**/*.java", "**/*.kt", "**/*.groovy", "**/*.html", "**/*.jsp"
  )

  fun promptProp(prop: String): () -> String {
    return promptProp(prop, {
      throw ForkException("Fork property named '$prop' has no value provided.")
    })
  }

  fun promptProp(prop: String, defaultProvider: () -> String): () -> String {
    prompts[prop] = defaultProvider

    return { props[prop] ?: throw ForkException("Fork prompt property '$prop' has no value provided.") }
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
    val props = Properties()
    props.load(FileInputStream(project.file("fork.properties")))

    return prompts.mapValues({ (prop, defaultValue) ->
      val value = props.getOrElse(prop, defaultValue)
      value as String
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

  fun moveFile(movements: Map<String, String>) {
    rule(MoveFileRule(this, movements.mapValues { promptTemplate(it.value) }))
  }

  fun replaceContent(replacements: Map<String, String>, configurer: Closure<*>) {
    rule(ReplaceContentRule(this, replacements.mapValues { promptTemplate(it.value) }), configurer)
  }

  fun replaceContent(replacements: Map<String, String>, filterInclude: String) {
    replaceContent(replacements, listOf(filterInclude))
  }

  fun replaceContent(replacements: Map<String, String>) {
    replaceContent(replacements, textFiles)
  }

  fun replaceContent(replacements: Map<String, String>, filterIncludes: Iterable<String>) {
    val rule = ReplaceContentRule(this, replacements.mapValues { promptTemplate(it.value) })
    rule.filter.include(filterIncludes)
    rule(rule)
  }

  fun clean() {
    rule(CleanRule(this))
  }

  override fun toString(): String {
    return "Config(name=$name,ruleCount=${rules.size})"
  }

}
