package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.process.Process
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

open class ForkTask @Inject constructor(
  private val workerExecutor: WorkerExecutor
) : DefaultTask() {

  init {
    outputs.upToDateWhen { false }
  }

  @Input
  private val configs = mutableMapOf<File, Config>()

  @TaskAction
  fun fork() {
    if (configs.isEmpty()) {
      throw ForkException("No fork configurations defined")
    }

    processConfigs()
  }

  private fun processConfigs() {


    for (config in configs) {
      workerExecutor.submit(Process::class.java) { c ->
        c.isolationMode = IsolationMode.AUTO
        c.params(config)
      }
    }
  }

  fun config(closure: Closure<*>) {
    config(project.rootDir, closure)
  }

  fun config(path: String, closure: Closure<*>) {
    config(project.file(path), closure)
  }

  fun config(root: File, closure: Closure<*>) {
    ConfigureUtil.configure(closure, configs.getOrPut(root, {
      Config(project, project.fileTree(root))
    }))
  }

  companion object {
    val NAME = "fork"
  }
}
