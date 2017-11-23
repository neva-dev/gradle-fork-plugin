package com.neva.gradle.fork

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.process.Process
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

open class ForkTask : DefaultTask {

  @Input
  private val configs = mutableListOf<Config>()

  private val workerExecutor: WorkerExecutor

  @Inject constructor(workerExecutor: WorkerExecutor) : super() {
    this.workerExecutor = workerExecutor
    this.outputs.upToDateWhen { false }
  }

  companion object {
    val NAME = "fork"
  }

  @TaskAction
  fun fork() {
    logger.info("Forking executed")

    for (config in configs) {
      workerExecutor.submit(Process::class.java) { config ->
        config.isolationMode = IsolationMode.AUTO
        config.params(config)
      }
    }


  }

}
