package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.tasks.ConfigTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Allows to define and execute forking configurations and also use base API.
 *
 * Dedicated to be used only at root project. For subprojects, apply plugin 'com.neva.fork.props'.
 */
open class ForkPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      plugins.apply(PropsPlugin::class.java)

      // Register fork DSL API
      val extension = extensions.create(
        ForkExtension.NAME, ForkExtension::class.java,
        project, project.extensions.getByType(PropsExtension::class.java)
      )

      // Predefine configurations
      extension.apply {
        config(Config.NAME_DEFAULT).apply {
          project.tasks.register(Config.NAME_DEFAULT, ConfigTask::class.java) {
            it.description = "Generates new project basing on itself."
          }
        }

        properties(Config.NAME_PROPERTIES, "gradle.user.properties", Action {
          it.description = "Generates user specific 'gradle.properties' file basing on template and prompted values."
        }, Action {
          it.description = "Requires having generated user specific 'gradle.properties' file."
        })
      }
    }
  }
}
