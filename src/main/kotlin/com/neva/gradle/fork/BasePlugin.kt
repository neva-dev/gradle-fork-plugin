package com.neva.gradle.fork

import org.gradle.api.Plugin
import org.gradle.api.Project

open class BasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.extensions.create(BaseExtension.NAME, BaseExtension::class.java, project)
  }

}
