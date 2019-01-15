package com.neva.gradle.fork

import com.neva.gradle.fork.tasks.Fork
import com.neva.gradle.fork.tasks.Props
import org.gradle.api.Plugin
import org.gradle.api.Project

open class ConfigPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.extensions.create(ConfigExtension.NAME, ConfigExtension::class.java, project)
    project.tasks.register(Fork.NAME, Fork::class.java)
    project.tasks.register(Props.NAME, Props::class.java)
  }

}
