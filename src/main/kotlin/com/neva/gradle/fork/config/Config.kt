package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkCancelException
import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.ForkExtension
import com.neva.gradle.fork.config.properties.*
import com.neva.gradle.fork.config.rule.*
import com.neva.gradle.fork.gui.PropertyDialog
import com.neva.gradle.fork.template.TemplateEngine
import nu.studer.java.util.OrderedProperties
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.file.FileTree
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Represents set of rules that are using properties to customize files (rename files / dirs, update content etc).
 */
@Suppress("TooManyFunctions")
abstract class Config(val fork: ForkExtension, val name: String) {

  val project = fork.project

  val description = project.objects.property(String::class.java).apply {
    convention(project.provider { "Configuration '$name'" })
  }

  val cacheDir = project.objects.directoryProperty().apply {
    convention(project.layout.projectDirectory.dir(".gradle/fork/config"))
  }

  val cacheName = project.objects.property(String::class.java).apply {
    convention(name)
  }

  val propsFile = project.objects.fileProperty().apply {
    set(project.file(project.findProperty("fork.properties")?.toString() ?: "fork.properties"))
  }

  private val rules = mutableListOf<Rule>()

  private val prompts = mutableMapOf<String, PropertyPrompt>()

  private val promptedProperties by lazy { promptFill() }

  val definedProperties: List<Property> by lazy { propsDefine() }

  abstract val sourcePath: String

  val sourceDir: File get() = File(sourcePath)

  val sourceTree: FileTree get() = project.fileTree(sourcePath)

  abstract val targetPath: String

  val targetDir: File get() = File(targetPath)

  val targetTree: FileTree get() = project.fileTree(targetPath)

  val textFiles = project.objects.listProperty(String::class.java).apply {
    set(listOf(
      "**/*.gradle", "**/*.xml", "**/*.properties", "**/*.js", "**/*.json", "**/*.css", "**/*.scss",
      "**/*.java", "**/*.kt", "**/*.kts", "**/*.groovy", "**/*.html", "**/*.jsp"
    ))
  }

  val executableFiles = project.objects.listProperty(String::class.java).apply {
    set(listOf(
      "**/*.sh",
      "**/*.bat",
      "**/gradlew",
      "**/mvnw"
    ))
  }

  val textIgnoredFiles = project.objects.listProperty(String::class.java).apply {
      set(listOf(
        "**/.gradle/*", "**/build/*", "**/node_modules/*", "**/.git/*"
      ))
  }

  val templateDir = project.objects.directoryProperty().apply {
    convention(project.layout.projectDirectory.dir("gradle/fork"))
  }

  val templateEngine = TemplateEngine(fork)

  fun findTemplateFile(templateName: String): File? {
    val pebFile = templateDir.get().asFile.resolve("$templateName.peb")
    val regularFile = templateDir.get().asFile.resolve(templateName)
    return listOf(pebFile, regularFile).firstOrNull { it.exists() }
  }

  fun getSourceFile(sourceName: String) = sourceDir.resolve(sourceName)

  fun getTargetFile(targetName: String) = targetDir.resolve(targetName)

  private val previousPropsFile get() = cacheDir.get().asFile.resolve("${cacheName.get()}.properties")

  private val logger = project.logger

  fun promptProp(prop: String, defaultProvider: () -> String?): () -> String {
    prompts[prop] = PropertyPrompt(prop, defaultProvider)

    return { promptedProperties[prop] ?: throw ForkException("Fork prompt property '$prop' not bound.") }
  }

  fun promptProp(prop: String): () -> String = promptProp(prop) { null }

  fun promptTemplate(template: String): () -> String {
    templateEngine.parse(template).forEach { prop ->
      prompts[prop] = PropertyPrompt(prop)
    }

    return { renderTemplate(template) }
  }

  private fun promptDynamicProperties() {
    fork.propertyDefinitions.all.filter { definition ->
      definition.dynamic.any { configPattern -> FilenameUtils.wildcardMatch(this.name, configPattern) }
    }.forEach {
      promptProp(it.name)
    }
  }

  fun renderTemplate(template: String) = templateEngine.render(template, promptedProperties)

  private fun propsDefine(): List<Property> {
    val others = mutableMapOf<String, Property>()
    val context = PropertyContext(others)

    prompts.forEach { (name, prompt) ->
      val definition = fork.propertyDefinitions.get(prompt.name) ?: PropertyDefinition(prompt.name)
      val property = Property(definition, prompt, context)

      others[name] = property
    }

    return others.values.sortedBy { p -> fork.propertyDefinitions.indexOf(p.name) }.toList()
  }

  private fun promptFill(): Map<String, String?> {
    promptDynamicProperties()
    if (fork.cached.get()) {
      promptFillPropertiesFile(previousPropsFile)
    }
    try {
      promptFillPropertiesFile(propsFile.get().asFile)
      promptPreProcess()

      promptFillCommandLine()
      promptFillGui()

      promptValidate()
    } finally {
      promptPostProcess()
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
      val fileProps = OrderedProperties()
      fileProps.load(input)
      fileProps.entrySet().forEach { (p, v) -> prompts[p.toString()]?.value = v.toString() }
    }
  }

  private fun promptSavePropertiesFile(file: File) {
    file.parentFile.mkdirs()

    FileOutputStream(file).use { output ->
      val props = OrderedProperties()
      prompts.values.forEach { prompt -> prompt.valueOrDefault?.also { props.setProperty(prompt.name, it) } }
      props.store(output, null)
    }
  }

  private fun promptFillGui() {
    if (prompts.isEmpty()) {
      return
    }

    definedProperties.forEach { it.control() }

    if (fork.interactive.get()) {
      val dialog = PropertyDialog.make(this)
      dialog.props.forEach { (p, v) -> prompts[p]?.value = v }
      if (dialog.cancelled) {
        throw ForkCancelException("Configuration evaluation cancelled by interactive mode!")
      }
    } else {
      definedProperties.forEach { prompts[it.name]?.value = it.value }
    }
  }

  private fun promptValidate() {
    val invalidProps = definedProperties.filter(Property::invalid).map { it.name }
    if (invalidProps.isNotEmpty()) {
      throw ForkException("Configuration '$name' cannot be evaluated because of missing or invalid properties: $invalidProps." +
        " Specify them via properties file ${propsFile.get()} or interactive mode.")
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

  private fun <T : Rule> rule(rule: T, configurer: Action<in T>) {
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

  fun replaceTexts(replacements: Map<String, String>) {
    replaceTexts(replacements) {
      it.filter.include(textFiles.get())
      it.filter.exclude(textIgnoredFiles.get())
    }
  }

  fun replaceTexts(replacements: Map<String, String>, configurer: Action<in ReplaceContentsRule>) {
    rule(ReplaceContentsRule(this, replacements.mapValues { promptTemplate(it.value) }), configurer)
  }

  fun replaceText(search: String, replace: String) {
    replaceTexts(mapOf(search to replace))
  }

  fun removeText(removal: String) = removeTexts(listOf(removal))

  fun removeTexts(removals: List<String>) = replaceTexts(removals.map { it to "" }.toMap())

  fun eachFiles(action: Action<in FileHandler>) {
    rule(EachFilesRule(this, action))
  }

  fun eachFiles(action: Action<in FileHandler>, options: Action<in EachFilesRule>) {
    rule(EachFilesRule(this, action), options)
  }

  fun replaceContents(replacements: Map<String, String>) = replaceTexts(replacements)

  fun replaceContent(search: String, replace: String) = replaceText(search, replace)

  fun eachTextFiles(action: Action<in FileHandler>) {
    eachFiles(textFiles.get(), textIgnoredFiles.get(), action)
  }

  fun eachTextFiles(pattern: String, action: Action<in FileHandler>) = eachTextFiles(listOf(pattern), action)

  fun eachTextFiles(patterns: List<String>, action: Action<in FileHandler>) {
    eachFiles(patterns, textIgnoredFiles.get(), action)
  }

  fun eachFiles(includes: List<String>, excludes: List<String>, action: Action<in FileHandler>) {
    eachFiles(action, {
      it.filter.include(includes)
      it.filter.exclude(excludes)
    })
  }

  fun removeFile(path: String) = removeFiles(listOf(path))

  fun removeFiles(includes: List<String>, excludes: List<String> = listOf(), cleanEmptyDirs: Boolean = true) {
    eachFiles(includes, excludes) { it.remove() }
    if (cleanEmptyDirs) {
      removeEmptyDirs()
    }
  }

  fun removeEmptyDirs() {
    action { it.removeEmptyDirs() }
  }

  fun copyTemplateFile(templateName: String, options: CopyTemplateFilesRule.() -> Unit = {}) {
    copyTemplateFile(templateName, templateName, options)
  }

  fun copyTemplateFile(templateName: String, targetName: String, options: CopyTemplateFilesRule.() -> Unit = {}) {
    copyTemplateFiles(mapOf(templateName to targetName), options)
  }

  fun copyTemplateFiles(files: Map<String, String>, options: CopyTemplateFilesRule.() -> Unit = {}) {
    rule(CopyTemplateFilesRule(this, files).apply(options))
  }

  fun convertProperties(sourceName: String, options: ConvertPropertiesRule.() -> Unit) {
    rule(ConvertPropertiesRule(this, sourceName).apply(options))
  }

  fun makeFilesExecutable(includes: List<String> = executableFiles.get(), excludes: List<String> = listOf()) {
    eachFiles(includes, excludes) { it.makeExecutable() }
  }

  fun action(executor: Action<in ActionRule>) {
    rule(ActionRule(this, {}, executor))
  }

  fun action(validator: Action<in ActionRule>, executor: Action<in ActionRule>) {
    rule(ActionRule(this, validator, executor))
  }

  fun evaluate() = try {
    validate()
    execute()
  } catch (e: ForkCancelException) {
    if (fork.verbose.get()) {
      throw e
    } else {
      logger.lifecycle(e.message)
    }
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

  override fun toString(): String = "Config(name='$name')"

  companion object {
    const val NAME_FORK = "fork"
    const val NAME_PROPERTIES = "props"
  }
}
