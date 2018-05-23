package com.neva.gradle.fork

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class PropertiesTask : DefaultTask() {

  @Internal
  private val fork = ForkTask.of(project)

  init {
    description = "Generates user specific 'gradle.properties' file basing on template and prompted values."

    fork.inPlaceConfig(CONFIG_NAME, { copyTemplateFile(TEMPLATE_FILE) })
  }

  @TaskAction
  fun properties() {
    fork.config(CONFIG_NAME).evaluate()
  }

  companion object {

    const val NAME = "props"

    const val CONFIG_NAME = "properties"

    const val TEMPLATE_FILE = "gradle.properties"

  }

}
