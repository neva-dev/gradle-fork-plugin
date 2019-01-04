package com.neva.gradle.fork.config

import com.neva.gradle.fork.config.properties.PropertyDefinitions
import org.gradle.api.Project
import java.io.File

class SourceTargetConfig(project: Project, propertyDefinitions: PropertyDefinitions, name: String) : Config(project, propertyDefinitions, name) {

  override val sourcePath: String by lazy(promptProp("sourcePath") {
    project.projectDir.absolutePath
  })

  override val targetPath: String by lazy(promptProp("targetPath") {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  })

}
