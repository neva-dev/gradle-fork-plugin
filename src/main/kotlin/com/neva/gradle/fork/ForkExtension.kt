package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.InPlaceConfig
import com.neva.gradle.fork.config.SourceTargetConfig
import com.neva.gradle.fork.config.properties.PropertyDefinitions
import com.neva.gradle.fork.tasks.RequirePropertiesTask
import com.neva.gradle.fork.tasks.ForkTask
import com.neva.gradle.fork.tasks.PropertiesTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.internal.Actions
import java.io.File

open class ForkExtension(val project: Project, val props: PropsExtension) {

  private val logger = project.logger

  private val configs = mutableMapOf<String, Config>()

  val defaults = project.findProperty("fork.defaults")?.toString()?.toBoolean() ?: true

  val ci = project.objects.property(Boolean::class.java).apply {
    convention(false)
    project.findProperty("fork.ci")?.toString()?.toBoolean()?.let { set(it) }
  }

  /**
   * Do not use previously filled values on dialog (regardless configuration executed or not).
   */
  val cached = project.objects.property(Boolean::class.java).apply {
    convention(ci.map { !it })
    project.findProperty("fork.cached")?.toString()?.toBoolean()?.let { set(it) }
  }

  /**
   * Do not try to show GUI dialog. Expect to provide properties using '-PforkProp.name=value' or via
   */
  val interactive = project.objects.property(Boolean::class.java).apply {
    convention(ci.map { !it })
    project.findProperty("fork.interactive")?.toString()?.toBoolean()?.let { set(it) }
  }

  /**
   * Fail build when configuration is not executed after showing GUI dialog.
   */
  val verbose = project.objects.property(Boolean::class.java).apply {
    convention(ci)
    project.findProperty("fork.verbose")?.toString()?.toBoolean()?.let { set(it) }
  }

  /**
   * Override already defined properties (both gradle.properties and provided via CLI)
   */
  val override = project.objects.property(Boolean::class.java).apply {
    convention(false)
    project.findProperty("fork.override")?.toString()?.toBoolean()?.let { set(it) }
  }

  fun config(name: String = Config.NAME_FORK, configurer: Action<in SourceTargetConfig> = Actions.doNothing()): Config {
    return configs.getOrPut(name) { SourceTargetConfig(this, name) }.apply {
      configurer.execute(this as SourceTargetConfig)
    }
  }

  fun inPlaceConfig(name: String, configurer: Action<in InPlaceConfig> = Actions.doNothing()): Config {
    return configs.getOrPut(name) { InPlaceConfig(this, name) }.apply {
      configurer.execute(this as InPlaceConfig)
    }
  }

  @Internal
  val propertyDefinitions = PropertyDefinitions(this)

  fun properties(action: Action<in PropertyDefinitions>) {
    action.execute(propertyDefinitions)
  }

  fun loadProperties(file: File, override: Boolean = this.override.get()) {
    if (!file.exists()) {
      logger.debug("Properties not loaded as properties file does not exist '$file'!")
      return
    }

    logger.info("Loading properties from file '$file'")
    props.read(file).forEach { (name, value) ->
      when {
        name.startsWith(SYSTEM_PROP_PREFIX) -> System.setProperty(name.substringAfter(SYSTEM_PROP_PREFIX), value)
        else -> {
          val extraProperties = project.extensions.extraProperties
          if (override || !extraProperties.has(name)) {
            extraProperties.set(name, value)
          }
        }
      }
    }
  }

  fun loadProperties(filePath: String) = loadProperties(project.file(filePath))

  fun loadPropertiesFrom(dirPath: String) = loadPropertiesFrom(project.file(dirPath))

  fun loadPropertiesFrom(dir: File) {
    if (!dir.exists()) {
      logger.debug("Properties not loaded as properties directory does not exist '$dir'!")
      return
    }

    project.fileTree(dir)
      .matching { it.include("**/*.properties") }
      .sorted()
      .forEach { loadProperties(it) }
  }

  // Defining config and task at same time

  fun useForking(configName: String, options: Config.() -> Unit = {}) = config(Config.NAME_FORK).apply {
    project.tasks.register(configName, ForkTask::class.java, this)
    options()
  }

  fun useProperties(configName: String, filePath: String, options: Config.() -> Unit = {}) = useProperties(configName, filePath, filePath, options)

  fun useProperties(configName: String, fileSourcePath: String, fileTargetPath: String, options: Config.() -> Unit = {}) = inPlaceConfig(configName).apply {
    copyTemplateFile(fileSourcePath, fileTargetPath)
    loadProperties(fileTargetPath)
    options()

    project.tasks.register(name, PropertiesTask::class.java, this)
    project.tasks.register("require${name.capitalize()}", RequirePropertiesTask::class.java, this, listOf(fileTargetPath))
  }

  fun generateProperties(configName: String, filePath: String, options: Config.() -> Unit = {}): Config {
    return generateProperties(configName, mapOf(filePath to filePath), options)
  }

  fun generateProperties(configName: String, files: Iterable<String>, options: Config.() -> Unit = {}): Config {
    return generateProperties(configName, files.map { it to it }.toMap(), options)
  }

  fun generateProperties(configName: String, files: Map<String, String>, options: Config.() -> Unit = {}): Config {
    return inPlaceConfig(configName).apply {
      copyTemplateFiles(files)
      options()

      project.tasks.register(name, PropertiesTask::class.java, this)
      project.tasks.register("require${name.capitalize()}", RequirePropertiesTask::class.java, this, files.values)
    }
  }

  init {
    if (defaults) {
      useForking(Config.NAME_FORK)
      useProperties(Config.NAME_PROPERTIES, "gradle.user.properties")
      loadPropertiesFrom("gradle/user")
    }
  }

  companion object {

    const val NAME = "fork"

    const val TASK_GROUP = "fork"

    const val SYSTEM_PROP_PREFIX = "systemProp."

    fun of(project: Project) = project.extensions.getByType(ForkExtension::class.java)
  }
}

val Project.fork get() = ForkExtension.of(this)
