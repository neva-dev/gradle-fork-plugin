package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkExtension
import java.io.File

class SourceTargetConfig(forkExtension: ForkExtension, name: String) : Config(forkExtension, name) {

  override val sourcePath: String by lazy(promptProp("sourcePath") {
    project.projectDir.absolutePath
  })

  override val targetPath: String by lazy(promptProp("targetPath") {
    File(project.rootDir.parentFile, "${project.rootDir.name}-fork").absolutePath
  })

}
