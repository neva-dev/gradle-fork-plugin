package com.neva.gradle.fork.template.filter

import com.mitchellbosecke.pebble.extension.Filter
import org.apache.commons.lang3.StringUtils

class ReplaceFilter : Filter {

  override fun getArgumentNames(): List<String>? {
    return listOf("search", "replace")
  }

  override fun apply(input: Any?, args: Map<String, Any>): Any? {
    return if (input is String) {
      val search = args["search"]
      val replace = args["replace"]
      if (search is String && replace is String) {
        StringUtils.replace(input, search, replace)
      } else {
        null
      }
    } else {
      null
    }
  }

}
