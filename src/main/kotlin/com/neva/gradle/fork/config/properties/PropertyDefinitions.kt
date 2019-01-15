package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ConfigExtension
import org.gradle.api.Action
import org.gradle.internal.Actions

class PropertyDefinitions(val fork: ConfigExtension) {

  private val definitions = mutableMapOf<String, PropertyDefinition>()

  fun define(name: String, action: Action<in PropertyDefinition>) {
    define(name) { Actions.with(this, action) }
  }

  fun define(name: String, options: PropertyDefinition.() -> Unit) {
    definitions += (name to fork.project.objects.newInstance(PropertyDefinition::class.java, name).apply(options))
  }

  fun define(definitions: Map<String, PropertyDefinition.() -> Unit>) {
    definitions.forEach { name, options -> define(name, options) }
  }

  fun get(name: String): PropertyDefinition? = definitions[name]

}
