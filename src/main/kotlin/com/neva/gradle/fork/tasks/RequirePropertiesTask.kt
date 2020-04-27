package com.neva.gradle.fork.tasks

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.ForkExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import com.neva.gradle.fork.config.Config

open class RequirePropertiesTask @Inject constructor(private val config: Config, private val filePath: String) : DefaultTask() {

  init {
    description = "Requires having generated user-specific properties file"
    group = ForkExtension.TASK_GROUP
  }

  @TaskAction
  fun evaluate() {
    if (!config.getTargetFile(filePath).exists()) {
      throw ForkException("Required properties file '$filePath' does not exist!\n" +
        "Run task '${config.name}' to generate it interactively.")
    }
  }
}
