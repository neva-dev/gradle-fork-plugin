package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkException
import org.apache.commons.io.FilenameUtils

class PropertyContext(private val properties: Map<String, Property>) {

  fun get(name: String): Property = properties[name] ?: throw ForkException("Property named '$name' does not exist.")

  fun find(pattern: String): List<Property> = properties.filter {
    FilenameUtils.wildcardMatch(it.key, pattern)
  }.map { it.value }

}
