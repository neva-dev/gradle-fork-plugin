package com.neva.gradle.fork.template

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Filter
import com.neva.gradle.fork.template.filter.SubstituteFilter

class TemplateExtension : AbstractExtension() {

  override fun getFilters(): MutableMap<String, Filter> {
    return mutableMapOf(
      "substitute" to SubstituteFilter()
    )
  }
}
