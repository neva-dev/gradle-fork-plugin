package com.neva.gradle.fork.config

import org.gradle.api.Project
import java.io.File

class SourceTargetConfig(project: Project, name: String) : Config(project, name) {

  override val sourcePath: String by lazy(promptProp("sourcePath") {
    project.projectDir.absolutePath
  })

  override val targetPath: String by lazy(promptProp("targetPath") {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  })

}
