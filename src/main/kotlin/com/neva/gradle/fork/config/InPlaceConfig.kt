package com.neva.gradle.fork.config

import org.gradle.api.Project

class InPlaceConfig(project: Project, name: String) : Config(project, name) {

  override val sourcePath: String by lazy { project.projectDir.absolutePath }

  override val targetPath: String by lazy { sourcePath }

}
