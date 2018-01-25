package com.neva.gradle.fork.template

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.lexer.Syntax
import com.mitchellbosecke.pebble.loader.StringLoader
import org.apache.commons.lang3.text.StrSubstitutor
import java.io.StringWriter
import java.util.regex.Pattern

class TemplateEngine {

  fun render(template: String, props: Map<String, String>): String {
    val interpolated = INTERPOLATOR(template, props)
    val expanded = StringWriter()
    ENGINE.getTemplate(interpolated).evaluate(expanded, props)

    return expanded.toString()
  }

  fun parse(template: String): Map<String, String?> {
    val m = PROP_PATTERN.matcher(template)

    val result = mutableMapOf<String, String?>()
    while (m.find()) {
      val prop = m.group(1)

      if (prop.contains(PROP_DELIMITER)) {
        val parts = prop.split(PROP_DELIMITER)

        val defaultName = parts[0].trim()
        val defaultValue = parts.mapNotNull {
          val match = DEFAULT_REGEX.matchEntire(it.trim())
          if (match != null) {
            match.groupValues[1]
          } else {
            null
          }
        }.firstOrNull()

        result[defaultName] = defaultValue
      } else {
        result[prop] = null
      }
    }

    return result
  }

  companion object {

    private val PROP_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}")

    private val PROP_DELIMITER = "|"

    private val DEFAULT_REGEX = Regex("default\\('([^']*)'\\)")

    private val VAR_PREFIX = "{{"

    private val VAR_SUFFIX = "}}"

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

    private val INTERPOLATOR: (String, Map<String, Any>) -> String = { source, props ->
      StrSubstitutor.replace(source, props, VAR_PREFIX, VAR_SUFFIX)
    }

  }
}
