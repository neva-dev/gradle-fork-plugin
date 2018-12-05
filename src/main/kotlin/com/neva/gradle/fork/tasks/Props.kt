package com.neva.gradle.fork.tasks

import org.gradle.api.tasks.TaskAction

open class Props : DefaultTask() {

  init {
    description = "Generates user specific 'gradle.properties' file basing on template and prompted values."

    ext.inPlaceConfig(CONFIG_NAME) { copyTemplateFile(TEMPLATE_FILE) }
  }

  @TaskAction
  fun properties() {
    ext.config(CONFIG_NAME).evaluate()
  }

  companion object {

    const val NAME = "props"

    const val CONFIG_NAME = "properties"

    const val TEMPLATE_FILE = "gradle.properties"

  }

}
