package com.neva.gradle.fork.config

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.lexer.Syntax
import com.mitchellbosecke.pebble.loader.StringLoader
import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.rule.*
import com.neva.gradle.fork.gui.PropertyDialog
import groovy.lang.Closure
import org.apache.commons.lang3.text.StrSubstitutor
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.util.ConfigureUtil
import java.io.File
import java.io.FileInputStream
import java.io.StringWriter
import java.util.*
import java.util.regex.Pattern

class Config(val project: Project, val name: String) {

  val prompts = mutableMapOf<String, PropertyPrompt>()

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

  var templateDir: File = project.file("gradle/fork")

  fun promptProp(prop: String, defaultProvider: () -> String?): () -> String {
    prompts[prop] = PropertyPrompt(prop, defaultProvider)

    return { props[prop] ?: throw ForkException("Fork prompt property '$prop' not bound.") }
  }

  fun promptProp(prop: String): () -> String {
    return promptProp(prop, { null })
  }

  fun promptTemplate(template: String): () -> String {
    parseTemplate(template).forEach { prop, defaultValue ->
      prompts[prop] = PropertyPrompt(prop, { defaultValue })
    }

    return { renderTemplate(template) }
  }

  private fun parseTemplate(template: String): Map<String, String?> {
    val m = TEMPLATE_PROP_PATTERN.matcher(template)

    val result = mutableMapOf<String, String?>()
    while (m.find()) {
      val prop = m.group(1)

      if (prop.contains(TEMPLATE_PROP_DELIMITER)) {
        val parts = prop.split(TEMPLATE_PROP_DELIMITER)

        val defaultName = parts[0].trim()
        val defaultValue = parts.mapNotNull {
          val match = TEMPLATE_DEFAULT_REGEX.matchEntire(it.trim())
          if (match != null) {
            match.groupValues[1]
          } else {
            null
          }
        }.firstOrNull()

        result[defaultName] = defaultValue
      } else {
        result[prop] = null
      }
    }

    return result
  }

  fun renderTemplate(template: String): String {
    val interpolated = TEMPLATE_INTERPOLATOR(template, props)
    val expanded = StringWriter()
    TEMPLATE_ENGINE.getTemplate(interpolated).evaluate(expanded, props)

    return expanded.toString()
  }

  private fun promptFill(): Map<String, String> {
    // Fill from properties file
    val propsFileSpecified = project.properties.containsKey("fork.properties")
    val propsFile = project.file(project.properties.getOrElse("fork.properties", { "fork.properties" }) as String)

    if (propsFile.exists()) {
      val fileProps = Properties()
      fileProps.load(FileInputStream(propsFile))
      fileProps.forEach { p, v -> prompts[p.toString()]!!.value = v.toString() }
    } else if (propsFileSpecified) {
      throw ForkException("Fork properties file does not exist: $propsFile")
    }

    // Fill missing by GUI
    val interactiveForced = (project.properties["fork.interactive"] as String?)?.toBoolean()
      ?: false
    val interactiveSpecified = project.properties.containsKey("fork.interactive")

    if (interactiveForced || (!interactiveSpecified && !prompts.values.any { it.valid })) {
      val guiProps = PropertyDialog.prompt(this, prompts.values.toList())
      guiProps.forEach { p, v -> prompts[p]!!.value = v }
    }

    // Revalidate missing props
    val invalidProps = prompts.values.filter { !it.valid }.map { it.name }
    if (invalidProps.isNotEmpty()) {
      throw ForkException("Fork cannot be performed, because of missing properties: $invalidProps."
        + " Specify them via properties file $propsFile or interactive mode.")
    }

    return prompts.mapValues { it.value.valueOrDefault }
  }

  private fun rule(rule: Rule, configurer: Closure<*>) {
    ConfigureUtil.configure(configurer, rule)
    rules += rule
  }

  private fun rule(rule: Rule) {
    rules += rule
  }

  fun cloneFiles() {
    rule(CloneFilesRule(this))
  }

  fun cloneFiles(configurer: Closure<*>) {
    rule(CloneFilesRule(this), configurer)
  }

  fun moveFile(from: String, to: String) {
    moveFiles(mapOf(from to to))
  }

  fun moveFiles(movements: Map<String, String>) {
    rule(MoveFilesRule(this, movements.mapValues { promptTemplate(it.value) }))
  }

  fun replaceContents(replacements: Map<String, String>, configurer: Closure<*>) {
    rule(ReplaceContentsRule(this, replacements.mapValues { promptTemplate(it.value) }), configurer)
  }

  fun replaceContents(replacements: Map<String, String>, filterInclude: String) {
    replaceContents(replacements, listOf(filterInclude))
  }

  fun replaceContent(search: String, replace: String) {
    replaceContents(mapOf(search to replace))
  }

  fun replaceContents(replacements: Map<String, String>) {
    replaceContents(replacements, textFiles)
  }

  fun replaceContents(replacements: Map<String, String>, filterIncludes: Iterable<String>) {
    val rule = ReplaceContentsRule(this, replacements.mapValues { promptTemplate(it.value) })
    rule.filter.include(filterIncludes)
    rule(rule)
  }

  fun copyTemplateFile(templateName: String) {
    copyTemplateFile(templateName, templateName)
  }

  fun copyTemplateFile(templateName: String, targetName: String) {
    copyTemplateFiles(mapOf(templateName to targetName))
  }

  fun copyTemplateFiles(files: Map<String, String>) {
    rule(CopyTemplateFilesRule(this, files))
  }

  fun action(closure: Closure<*>) {
    rule(ActionRule(this, closure))
  }

  override fun toString(): String {
    return "Config(name=$name,ruleCount=${rules.size})"
  }

  companion object {
    const val NAME_DEFAULT = "default"

    private val TEMPLATE_PROP_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}")

    private val TEMPLATE_PROP_DELIMITER = "|"

    private val TEMPLATE_DEFAULT_REGEX = Regex("default\\('([^']*)'\\)")

    private val TEMPLATE_VAR_PREFIX = "{{"

    private val TEMPLATE_VAR_SUFFIX = "}}"

    private val TEMPLATE_ENGINE = PebbleEngine.Builder()
      .autoEscaping(false)
      .cacheActive(false)
      .strictVariables(true)
      .newLineTrimming(false) // TODO pebble is trimming new line in gradle.properties if no value specified
      .loader(StringLoader())
      .syntax(Syntax.Builder()
        .setPrintOpenDelimiter(TEMPLATE_VAR_PREFIX)
        .setPrintCloseDelimiter(TEMPLATE_VAR_SUFFIX)
        .build()
      )
      .build()

    private val TEMPLATE_INTERPOLATOR: (String, Map<String, Any>) -> String = { source, props ->
      StrSubstitutor.replace(source, props, TEMPLATE_VAR_PREFIX, TEMPLATE_VAR_SUFFIX)
    }
  }

}
