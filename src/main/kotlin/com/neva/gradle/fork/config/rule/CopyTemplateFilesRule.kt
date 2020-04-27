package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler

class CopyTemplateFilesRule(config: Config, private val files: Map<String, String>) : AbstractRule(config) {

  init {
    registerPromptsForTemplateFiles()
  }

  private fun registerPromptsForTemplateFiles() {
    files.keys.forEach { templateName ->
      val templateFile = config.findTemplateFile(templateName)
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
      val templateFile = config.findTemplateFile(templateName)
      if (templateFile != null) {
        val targetFile = config.getTargetFile(targetName)

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
