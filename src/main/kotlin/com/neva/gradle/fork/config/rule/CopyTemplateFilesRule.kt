package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import java.io.File

class CopyTemplateFilesRule(config: Config, private val files: Map<String, String>) : AbstractRule(config) {

  init {
    registerPromptsForTemplateFiles()
  }

  private fun findTemplateFile(templateName: String): File? {
    val pebFile = File(config.templateDir, "$templateName.peb")
    val regularFile = File(config.templateDir, templateName)
    return listOf(pebFile, regularFile).firstOrNull { it.exists() }
  }

  private fun registerPromptsForTemplateFiles() {
    files.keys.forEach { templateName ->
      val templateFile = findTemplateFile(templateName)
      if (templateFile != null) {
        val template = FileHandler(config, templateFile).read()

        config.promptTemplate(template)
      }
    }
  }

  override fun execute() {
    copyAndExpandTemplateFiles()
  }

  private fun copyAndExpandTemplateFiles() {
    files.forEach { (templateName, targetName) ->
      val templateFile = findTemplateFile(templateName)
      if (templateFile != null) {
        val targetFile = File(config.targetDir, targetName)

        FileHandler(config, templateFile).copy(targetFile).perform()
        FileHandler(config, targetFile).expand()
      } else {
        logger.warn("Fork template file does not exist: $templateName")
      }
    }
  }

  override fun toString(): String {
    return "CopyTemplateFilesRule(files=$files)"
  }
}
