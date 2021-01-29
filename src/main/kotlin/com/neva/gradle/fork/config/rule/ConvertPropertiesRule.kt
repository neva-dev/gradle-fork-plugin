package com.neva.gradle.fork.config.rule

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.AbstractRule
import com.neva.gradle.fork.config.Config
import nu.studer.java.util.OrderedProperties
import java.io.File

class ConvertPropertiesRule(config: Config, private val sourcePath: String) : AbstractRule(config) {

  private val fileConverter = mutableListOf<Converter>()

  private var pairConverter: (Pair<String, String>) -> Pair<String, String> = { it }

  fun eachPair(props: Map<String, String>, consumer: (Pair<String, String>) -> Unit) {
      props.map { pairConverter(it.toPair()) }.forEach(consumer)
  }

  fun updatePair(converter: (Pair<String, String>) -> Pair<String, String>) {
    this.pairConverter = converter
  }

  fun toCustom(targetPath: String, converter: (File, Map<String, String>) -> Unit) {
    fileConverter.add(Converter(targetPath, converter))
  }

  fun toYml() = toYml("${sourcePath.substringBeforeLast(".")}.yml")

  fun toYml(targetPath: String) = toCustom(targetPath) { file, props ->
    file.printWriter().use { printer ->
      printer.println("---")
      eachPair(props) { (k, v) ->
        printer.println("$k: $v")
      }
    }
  }

  fun toTfVars() = toTfVars("${sourcePath.substringBeforeLast(".")}.tfvars")

  fun toTfVars(targetPath: String) = toCustom(targetPath) { file, props ->
    file.printWriter().use { printer ->
      eachPair(props) { (k, v) ->
        printer.println("$k = \"$v\"")
      }
    }
  }

  override fun execute() {
    val sourceFile = config.getSourceFile(sourcePath).takeIf { it.exists() }
      ?: throw ForkException("Properties source file '$sourcePath' does not exist!")
    val props = OrderedProperties().apply { sourceFile.bufferedReader().use { load(it) } }.run {
      propertyNames().asSequence().map { it to getProperty(it) }.toMap()
    }
    fileConverter.forEach { converter ->
      val targetFile = config.getTargetFile(converter.targetPath)
      converter.converter(targetFile, props)
    }
  }

  override fun toString(): String {
    return "ConvertProperties(sourcePath=$sourcePath)"
  }

  class Converter(val targetPath: String, val converter: (File, Map<String, String>) -> Unit)
}
