package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.config.Config
import org.gradle.api.tasks.TaskAction

open class Props : DefaultTask() {

  init {
    description = "Generates user specific 'gradle.properties' file basing on template and prompted values."

    ext.inPlaceConfig(Config.NAME_PROPERTIES) { copyTemplateFile(TEMPLATE_FILE) }
  }

  @TaskAction
  fun properties() {
    ext.config(Config.NAME_PROPERTIES).evaluate()
  }

  companion object {

    const val NAME = "props"

    const val TEMPLATE_FILE = "gradle.properties"
  }

}
