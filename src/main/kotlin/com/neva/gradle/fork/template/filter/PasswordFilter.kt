package com.neva.gradle.fork.template.filter

import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import com.neva.gradle.fork.PropsExtension

/**
 * Decrypts password on demand.
 */
class PasswordFilter(val props: PropsExtension) : Filter {
  override fun apply(input: Any, args: MutableMap<String, Any>, self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any? {
    return if (input is String) {
      props.encryptor.decrypt(input)
    } else {
      null
    }
  }

  override fun getArgumentNames(): List<String>? {
    return listOf()
  }
}
