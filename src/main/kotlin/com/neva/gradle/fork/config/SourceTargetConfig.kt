package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.properties.PropertiesDefinitions
import org.gradle.api.Project
import java.io.File

class SourceTargetConfig(project: Project, propertiesDefinitions: PropertiesDefinitions, name: String) : Config(project, propertiesDefinitions, name) {

  override val sourcePath: String by lazy(promptProp("sourcePath") {
    project.projectDir.absolutePath
  })

  override val targetPath: String by lazy(promptProp("targetPath") {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  })

}
