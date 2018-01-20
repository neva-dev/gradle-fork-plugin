package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.rule.ActionRule
import com.neva.gradle.fork.config.rule.CopyFileRule
import com.neva.gradle.fork.config.rule.MoveFileRule
import com.neva.gradle.fork.config.rule.ReplaceContentRule
import com.neva.gradle.fork.gui.PropsDialog
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

  fun promptProp(prop: String, defaultProvider: () -> String): () -> String {
    prompts[prop] = defaultProvider

    return { props[prop] ?: throw ForkException("Fork prompt property '$prop' not bound.") }
  }

  fun promptProp(prop: String): () -> String {
    return promptProp(prop, {
      throw PropertyException("Fork prompt property '$prop' has no value provided.")
    })
  }

  fun promptTemplate(template: String): () -> String {
    parseTemplate(template).forEach { prop ->
      prompts[prop] = {
        throw PropertyException("Fork template property '$prop' has no value provided.")
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
    val result = mutableMapOf<String, String>()

    // Evaluate defaults
    prompts.forEach { (prop, defaultValue) ->
      try {
        result[prop] = defaultValue()
      } catch (e: PropertyException) {
        result[prop] = ""
      }
    }

    // Fill from properties file
    val propsFileSpecified = project.properties.containsKey("fork.properties")
    val propsFile = project.file(project.properties.getOrElse("fork.properties", { "fork.properties" }) as String)

    if (propsFile.exists()) {
      val fileProps = Properties()
      fileProps.load(FileInputStream(propsFile))
      fileProps.forEach { k, v -> result[k.toString()] = v.toString() }
    } else if (propsFileSpecified) {
      throw ForkException("Fork properties file does not exist: $propsFile")
    }

    // Fill missing by GUI
    var missingProps = result.filter { it.value.isBlank() }
    val interactiveForced = (project.properties["fork.interactive"] as String?)?.toBoolean() ?: false
    val interactiveSpecified = project.properties.containsKey("fork.interactive")

    if (interactiveForced || (!interactiveSpecified && missingProps.isNotEmpty())) {
      result.putAll(PropsDialog.prompt(result))
    }

    // Validate missing again
    missingProps = result.filter { it.value.isBlank() }

    if (missingProps.isNotEmpty()) {
      throw ForkException("Fork cannot be performed, because of missing properties: ${missingProps.keys}."
        + " Specify them via properties file $propsFile or interactive mode.")
    }

    return result
  }

  private fun rule(rule: Rule, configurer: Closure<*>) {
    ConfigureUtil.configure(configurer, rule)
    rules += rule
  }

  private fun rule(rule: Rule) {
    rules += rule
  }

  fun copyFile() {
    rule(CopyFileRule(this))
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

  fun action(closure: Closure<*>) {
    rule(ActionRule(this, closure))
  }

  override fun toString(): String {
    return "Config(name=$name,ruleCount=${rules.size})"
  }

}
