package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
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
        config(Config.NAME_DEFAULT, Action {}).configure {
          it.description = "Generates new project basing on itself."
        }
        inPlaceConfig(Config.NAME_PROPERTIES, Action {
          it.copyTemplateFile("gradle.properties")
        }).configure {
          it.description = "Generates user specific 'gradle.properties' file basing on template and prompted values."
        }
        loadProperties()
      }
    }
  }
}
