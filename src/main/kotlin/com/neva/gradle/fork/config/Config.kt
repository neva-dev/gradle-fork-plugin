package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.rule.*
import com.neva.gradle.fork.gui.PropertyDialog
import com.neva.gradle.fork.template.TemplateEngine
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.util.ConfigureUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

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

  val templateEngine = TemplateEngine()

  var propsFile = project.file(project.properties.getOrElse("fork.properties", { "fork.properties" }) as String)

  private val propsFileSpecified = project.properties.containsKey("fork.properties")

  private val interactive = flag("fork.interactive", true)

  private fun flag(prop: String, defaultValue: Boolean = false): Boolean {
    val value = project.properties[prop] as String? ?: return defaultValue

    return if (!value.isBlank()) value.toBoolean() else true
  }

  fun promptProp(prop: String, defaultProvider: () -> String?): () -> String {
    prompts[prop] = PropertyPrompt(prop, defaultProvider)

    return { props[prop] ?: throw ForkException("Fork prompt property '$prop' not bound.") }
  }

  fun promptProp(prop: String): () -> String {
    return promptProp(prop, { null })
  }

  fun promptTemplate(template: String): () -> String {
    templateEngine.parse(template).forEach { prop, defaultValue ->
      prompts[prop] = PropertyPrompt(prop, { defaultValue })
    }

    return { renderTemplate(template) }
  }

  fun renderTemplate(template: String): String {
    return templateEngine.render(template, props)
  }

  private fun promptFill(): Map<String, String> {
    promptFillPropertiesFile()
    promptFillCommandLine()
    promptFillGui()
    promptValidate()
    promptSavePropertiesFile()

    return prompts.mapValues { it.value.valueOrDefault }
  }

  private fun promptFillCommandLine() {
    prompts.keys.forEach { prop ->
      val cmdProp = project.properties[prop]
      if (cmdProp is String) {
        prompts[prop]?.value = cmdProp
      }
    }
  }

  private fun promptFillPropertiesFile(): File {
    if (propsFile.exists()) {
      val fileProps = Properties()
      fileProps.load(FileInputStream(propsFile))
      fileProps.forEach { p, v -> prompts[p.toString()]?.value = v.toString() }
    } else if (propsFileSpecified) {
      throw ForkException("Fork properties file does not exist: $propsFile")
    }

    return propsFile
  }

  private fun promptFillGui() {
    if (interactive) {
      val guiProps = PropertyDialog.prompt(this, prompts.values.toList())
      guiProps.forEach { p, v -> prompts[p]?.value = v }
    }
  }

  private fun promptValidate() {
    val invalidProps = prompts.values.filter { !it.valid }.map { it.name }
    if (invalidProps.isNotEmpty()) {
      throw ForkException("Fork cannot be performed, because of missing properties: $invalidProps."
        + " Specify them via properties file $propsFile or interactive mode.")
    }
  }

  private fun promptSavePropertiesFile() {
    val props = Properties()
    prompts.values.forEach { prompt -> props[prompt.name] = prompt.valueOrDefault }
    props.store(FileOutputStream(propsFile), null)
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

  fun validate() {
    if (targetDir.exists()) {
      throw ForkException("Fork target directory already exists: ${targetDir.canonicalPath}")
    }
  }

  fun execute() {
    rules.forEach { it.execute() }
  }

  override fun toString(): String {
    return "Config(name=$name,rules=$rules)"
  }

  companion object {
    const val NAME_DEFAULT = "default"
  }

}
