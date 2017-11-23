package com.neva.gradle.fork.process

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import com.neva.gradle.fork.config.FileRule
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class Process @Inject constructor(
  private val project: Project,
  private val config: Config
) : Runnable {

  private val logger = project.logger

  private val logFile: File by lazy {
    val name = SimpleDateFormat("yyyy-MM-dd_hh-mm-ss'.log'").format(Date())

    File(project.buildDir, "fork/$name")
  }

  override fun run() {
    logger.info("Scanning files basing on $config")

    val handlers = config.scan()

    logger.info("Scanned total ${handlers.size} files or directories")

    logger.info("Applying path rules (${config.pathRules.size})")

    applyRules(handlers, config.pathRules)

    logger.info("Applying content rules (${config.contentRules.size})")
    applyRules(handlers, config.contentRules)

    logger.info("Dumping file changes to log file $logFile")
    dumpChanges(handlers)
  }

  private fun applyRules(handlers: List<FileHandler>, rules: MutableList<FileRule>) {
    handlers.forEach { h -> rules.forEach { it.apply(h) } }
  }

  private fun dumpChanges(handlers: List<FileHandler>) {
    GFileUtils.parentMkdirs(logFile)

    val logLines = mutableListOf(config.toString())
    logLines += handlers.flatMap { it.changes }
    val logContent = logLines.joinToString("\n")

    logFile.printWriter().use { it.print(logContent) }
  }

}
