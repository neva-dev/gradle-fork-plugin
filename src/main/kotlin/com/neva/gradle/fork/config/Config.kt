package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.ForkExtension
import com.neva.gradle.fork.config.properties.*
import com.neva.gradle.fork.config.rule.*
import com.neva.gradle.fork.gui.PropertyDialog
import com.neva.gradle.fork.template.TemplateEngine
import org.gradle.api.Action
import org.gradle.api.file.FileTree
import org.gradle.util.GFileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * Represents set of rules that are using properties to customize files (rename files / dirs, update content etc).
 */
abstract class Config(val fork: ForkExtension, val name: String) {

  val project = fork.project

  private val rules = mutableListOf<Rule>()

  private val prompts = mutableMapOf<String, PropertyPrompt>()

  private val promptedProperties by lazy { promptFill() }

  val definedProperties: List<Property> by lazy { propsDefine() }

  abstract val sourcePath: String

  val sourceDir: File
    get() = File(sourcePath)

  val sourceTree: FileTree
    get() = project.fileTree(sourcePath)

  abstract val targetPath: String

  val targetDir: File
    get() = File(targetPath)

  val targetTree: FileTree
    get() = project.fileTree(targetPath)

  var textFiles = mutableListOf(
    "**/*.gradle", "**/*.xml", "**/*.properties", "**/*.js", "**/*.json", "**/*.css", "**/*.scss",
    "**/*.java", "**/*.kt", "**/*.kts", "**/*.groovy", "**/*.html", "**/*.jsp"
  )

  var templateDir: File = project.file("gradle/fork")

  val templateEngine = TemplateEngine(project)

  var propsFile = project.file(project.properties.getOrElse("fork.properties") { "fork.properties" } as String)

  private val previousPropsFile = File(project.buildDir, "fork/config/$name.properties")

  private val interactive = flag("fork.interactive", true)

  private val logger = project.logger

  private fun flag(prop: String, defaultValue: Boolean = false): Boolean {
    val value = project.properties[prop] as String? ?: return defaultValue

    return if (!value.isBlank()) value.toBoolean() else true
  }

  fun promptProp(prop: String, defaultProvider: () -> String?): () -> String {
    prompts[prop] = PropertyPrompt(prop, defaultProvider)

    return { promptedProperties[prop] ?: throw ForkException("Fork prompt property '$prop' not bound.") }
  }

  fun promptProp(prop: String): () -> String {
    return promptProp(prop) { null }
  }

  fun promptTemplate(template: String): () -> String {
    templateEngine.parse(template).forEach { prop ->
      prompts[prop] = PropertyPrompt(prop)
    }

    return { renderTemplate(template) }
  }

  fun renderTemplate(template: String): String {
    return templateEngine.render(template, promptedProperties)
  }

  private fun propsDefine(): List<Property> {
    val others = mutableMapOf<String, Property>()
    val context = PropertyContext(others)

    prompts.forEach { name, prompt ->
      val definition = fork.propertyDefinitions.get(prompt.name) ?: PropertyDefinition(prompt.name)
      val property = Property(definition, prompt, context)

      others[name] = property
    }

    return others.values.sortedBy { p -> fork.propertyDefinitions.indexOf(p.name) }.toList()
  }

  private fun promptFill(): Map<String, String?> {
    promptFillPropertiesFile(previousPropsFile)

    try {
      promptFillPropertiesFile(propsFile)
      promptPreProcess()

      promptFillCommandLine()
      promptFillGui()

      promptValidate()
      promptPostProcess()
    } finally {
      promptSavePropertiesFile(previousPropsFile)
    }

    return prompts.mapValues { it.value.valueOrDefault }
  }

  private fun promptFillCommandLine() {
    prompts.keys.forEach { prop ->
      val cmdProp = project.properties["forkProp.$prop"]
      if (cmdProp is String) {
        prompts[prop]?.value = cmdProp
      }
    }
  }

  private fun promptFillPropertiesFile(file: File) {
    if (!file.exists()) {
      return
    }

    FileInputStream(file).use { input ->
      val fileProps = Properties()
      fileProps.load(input)
      fileProps.forEach { p, v -> prompts[p.toString()]?.value = v.toString() }
    }
  }

  private fun promptSavePropertiesFile(file: File) {
    GFileUtils.mkdirs(file.parentFile)

    FileOutputStream(file).use { output ->
      val props = Properties()
      prompts.values.forEach { prompt -> props[prompt.name] = prompt.valueOrDefault }
      props.store(output, null)
    }
  }

  private fun promptFillGui() {
    if (interactive && prompts.isNotEmpty()) {
      val dialog = PropertyDialog.make(this)
      dialog.props.forEach { p, v -> prompts[p]?.value = v }
      if (dialog.cancelled) {
        throw ForkException("Fork cancelled by interactive mode.")
      }
    }
  }

  private fun promptValidate() {
    val invalidProps = definedProperties.filter(Property::invalid).map { it.name }
    if (invalidProps.isNotEmpty()) {
      throw ForkException("Fork cannot be performed, because of missing or invalid properties: $invalidProps."
        + " Specify them via properties file $propsFile or interactive mode.")
    }
  }

  private fun promptPreProcess() {
    definedProperties.forEach { property ->
      if (property.type == PropertyType.PASSWORD) {
        prompts[property.name]?.apply { value = fork.props.encryptor.decrypt(value) }
      }
    }
  }

  private fun promptPostProcess() {
    definedProperties.forEach { property ->
      if (property.type == PropertyType.PASSWORD) {
         prompts[property.name]?.apply { value = fork.props.encryptor.encrypt(value) }
      }
    }
  }

  private fun <T: Rule> rule(rule: T, configurer: Action<in T>) {
    rules += rule.apply { configurer.execute(this) }
  }

  private fun rule(rule: Rule) {
    rules += rule
  }

  fun cloneFiles() {
    rule(CloneFilesRule(this))
  }

  fun cloneFiles(configurer: Action<in CloneFilesRule>) {
    rule(CloneFilesRule(this), configurer)
  }

  fun moveFile(from: String, to: String) {
    moveFiles(mapOf(from to to))
  }

  fun moveFiles(movements: Map<String, String>) {
    rule(MoveFilesRule(this, movements.mapValues { promptTemplate(it.value) }))
  }

  fun replaceContents(replacements: Map<String, String>, configurer: Action<in ReplaceContentsRule>) {
    rule(ReplaceContentsRule(this, replacements.mapValues { promptTemplate(it.value) }), configurer)
  }

  fun replaceContents(replacements: Map<String, String>, filterInclude: String) {
    replaceContents(replacements, listOf(filterInclude))
  }

  fun replaceContent(search: String, replace: String) {
    replaceContents(mapOf(search to replace))
  }

  fun eachFile(action: Action<in FileHandler>) {
    rule(EachFileRule(this, action))
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

  fun action(executor: Action<in ActionRule>) {
    rule(ActionRule(this, Action {}, executor))
  }

  fun action(validator: Action<in ActionRule>, executor: Action<in ActionRule>) {
    rule(ActionRule(this, validator, executor))
  }

  fun evaluate() {
    validate()
    execute()
  }

  fun validate() {
    logger.info("Validating $this")
    logger.debug("Effective properties: $promptedProperties")
    rules.forEach { it.validate() }
  }

  fun execute() {
    logger.info("Executing $this")
    rules.forEach { it.execute() }
  }

  override fun toString(): String {
    return "Config(name='$name')"
  }

  companion object {
    const val NAME_DEFAULT = "default"
    const val NAME_PROPERTIES = "properties"
  }

}
