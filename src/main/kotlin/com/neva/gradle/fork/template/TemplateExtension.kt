package com.neva.gradle.fork.template

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Filter
import com.neva.gradle.fork.PropsExtension
import com.neva.gradle.fork.template.filter.PasswordFilter
import com.neva.gradle.fork.template.filter.SubstituteFilter

class TemplateExtension(val props: PropsExtension) : AbstractExtension() {

  override fun getFilters(): MutableMap<String, Filter> {
    return mutableMapOf(
      "substitute" to SubstituteFilter(),
      "password" to PasswordFilter(props)
    )
  }
}
