package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.properties.PropertiesDefinitions
import org.gradle.api.Project

class InPlaceConfig(project: Project, propertiesDefinitions: PropertiesDefinitions, name: String) : Config(project, propertiesDefinitions, name) {

  override val sourcePath: String by lazy { project.projectDir.absolutePath }

  override val targetPath: String by lazy { sourcePath }

}
