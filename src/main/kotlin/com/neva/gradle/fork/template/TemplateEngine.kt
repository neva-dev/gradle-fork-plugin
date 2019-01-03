package com.neva.gradle.fork.template

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.lexer.Syntax
import com.mitchellbosecke.pebble.loader.StringLoader
import org.gradle.api.Project
import java.io.StringWriter
import java.util.regex.Pattern

class TemplateEngine(val project: Project) {

  private val projectProperties by lazy {
    mapOf("prop" to project.properties)
  }

  private val envProperties by lazy {
    mapOf("env" to System.getenv())
  }

  private val systemProperties: Map<String, Any> by lazy {
    val result = System.getProperties().entries.fold(mutableMapOf<String, String>(), { props, prop ->
      props[prop.key.toString()] = prop.value.toString(); props
    })

    mapOf("system" to result)
  }

  fun render(template: String, props: Map<String, Any?>): String {
    val effectiveProps = envProperties + systemProperties + projectProperties + props
    val renderer = StringWriter()
    ENGINE.getTemplate(template).evaluate(renderer, effectiveProps)

    return renderer.toString()
  }

  fun parse(template: String): List<String> {
    val m = PROP_PATTERN.matcher(template)

    val result = mutableListOf<String>()
    while (m.find()) {
      val prop = m.group(1)

      result += if (prop.contains(PROP_DELIMITER)) {
        val parts = prop.split(PROP_DELIMITER).map { it.trim() }
        parts[0]
      } else {
        prop
      }
    }

    return result
  }

  companion object {

    private val PROP_PATTERN = Pattern.compile("\\{\\{(.+?)}}")

    private const val PROP_DELIMITER = "|"

    private const val VAR_PREFIX = "{{"

    private const val VAR_SUFFIX = "}}"

    private val ENGINE = PebbleEngine.Builder()
      .extension(TemplateExtension())
      .autoEscaping(false)
      .cacheActive(false)
      .strictVariables(true)
      .newLineTrimming(false)
      .loader(StringLoader())
      .syntax(Syntax.Builder()
        .setEnableNewLineTrimming(false)
        .setPrintOpenDelimiter(VAR_PREFIX)
        .setPrintCloseDelimiter(VAR_SUFFIX)
        .build()
      )
      .build()

  }
}
