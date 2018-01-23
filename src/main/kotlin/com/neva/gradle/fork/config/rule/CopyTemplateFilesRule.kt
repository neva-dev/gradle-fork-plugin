package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.FileHandler
import java.io.File

class CopyTemplateFilesRule(config: Config, private val files: Map<String, String>) : AbstractRule(config) {

  init {
    registerPromptsForTemplateFiles()
  }

  private fun registerPromptsForTemplateFiles() {
    files.keys.forEach { templateName ->
      val templateFile = File(config.templateDir, templateName)
      if (!templateFile.exists()) {
        throw ForkException("Fork template file does not exist: $templateFile")
      }

      val template = FileHandler(config, templateFile).read()

      config.promptTemplate(template)
    }
  }

  override fun apply() {
    copyAndExpandTemplateFiles()
  }

  private fun copyAndExpandTemplateFiles() {
    files.forEach { templateName, targetName ->
      val templateFile = File(config.templateDir, templateName)
      val targetFile = File(config.targetDir, targetName)

      FileHandler(config, templateFile).copy(targetFile).perform()
      FileHandler(config, targetFile).expand()
    }
  }

}
