package com.neva.gradle.fork.core

import org.gradle.api.Plugin
import org.gradle.api.Project

class ForkPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.tasks.create(ForkTask.NAME, ForkTask::class.java)
  }

}
