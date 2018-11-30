package com.neva.gradle.fork.template.filter

import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.apache.commons.lang3.StringUtils

/**
 * There is 'replace' filter in core but takes a map as argument.
 */
class SubstituteFilter : Filter {
  override fun apply(input: Any, args: MutableMap<String, Any>, self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any? {
    return if (input is String) {
      val search = args["search"] ?: throw IllegalArgumentException("No search argument passed to substitute filter.")
      val replace = args["replace"] ?: throw IllegalArgumentException("No replace argument passed to substitute filter.")
      if (search is String && replace is String) {
        StringUtils.replace(input, search, replace)
      } else {
        null
      }
    } else {
      null
    }
  }

  override fun getArgumentNames(): List<String>? {
    return listOf("search", "replace")
  }

}
