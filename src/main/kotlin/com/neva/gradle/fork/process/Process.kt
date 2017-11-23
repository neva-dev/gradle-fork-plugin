package com.neva.gradle.fork.process

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import com.neva.gradle.fork.config.FileRule
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import java.io.File
import javax.inject.Inject

class Process @Inject constructor(
  private val project: Project, private val config: Config
) : Runnable {

  val logger = project.logger

  override fun run() {
    logger.info("Scanning files basing on $config")

    val handlers = config.scan()

    logger.info("Scanned total ${handlers.size} files or directories")

    logger.info("Applying path rules (${config.pathRules.size})")

    applyRules(handlers, config.pathRules)

    logger.info("Applying content rules (${config.contentRules.size})")
    applyRules(handlers, config.contentRules)

    logger.info("Dumping log to file...")
    dumpChanges(handlers)
  }

  private fun applyRules(handlers: List<FileHandler>, rules: MutableList<FileRule>) {
    handlers.forEach { h -> rules.forEach { it.apply(h) } }
  }

  private fun dumpChanges(handlers: List<FileHandler>) {
    val logFile = File(project.buildDir, "fork/${config.path}")
    GFileUtils.parentMkdirs(logFile)
    val logEntries = handlers.flatMap { it.changes }.joinToString("\n")

    logFile.printWriter().use { it.print(logEntries) }
  }

}
