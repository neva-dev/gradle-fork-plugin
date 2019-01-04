package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.properties.PropertyDefinitions
import org.gradle.api.Project

class InPlaceConfig(project: Project, propertyDefinitions: PropertyDefinitions, name: String) : Config(project, propertyDefinitions, name) {

  override val sourcePath: String by lazy { project.projectDir.absolutePath }

  override val targetPath: String by lazy { sourcePath }

}
