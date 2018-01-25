package com.neva.gradle.fork.template

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.extension.core.ReplaceFilter

class TemplateExtension : AbstractExtension() {

  override fun getFilters(): MutableMap<String, Filter> {
    return mutableMapOf(
      "replace" to ReplaceFilter()
    )
  }
}
