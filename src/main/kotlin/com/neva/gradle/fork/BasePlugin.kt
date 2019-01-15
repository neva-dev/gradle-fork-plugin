package com.neva.gradle.fork

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Allows to access encrypted properties (base / limited API).
 *
 * Dedicated to be used only in subprojects (to avoid redefining 'fork' and 'props' tasks).
 *
 * For root project, apply plugin 'com.neva.fork'.
 */
open class BasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.extensions.create(BaseExtension.NAME, BaseExtension::class.java, project)
  }

}
