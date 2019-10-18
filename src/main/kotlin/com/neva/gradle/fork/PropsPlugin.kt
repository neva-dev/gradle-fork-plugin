package com.neva.gradle.fork

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Allows to access encrypted properties.
 *
 * Dedicated to be used only in subprojects (to avoid redefining 'fork' and 'props' tasks).
 *
 * For root project, instead apply plugin 'com.neva.fork'.
 */
open class PropsPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      extensions.create(PropsExtension.NAME, PropsExtension::class.java, project)
    }
  }
}
