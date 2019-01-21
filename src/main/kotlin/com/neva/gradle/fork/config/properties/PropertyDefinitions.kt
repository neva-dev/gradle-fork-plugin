package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkExtension
import org.gradle.api.Action
import org.gradle.internal.Actions

class PropertyDefinitions(val fork: ForkExtension) {

  private val definitions = mutableMapOf<String, PropertyDefinition>()

  val all: List<PropertyDefinition>
    get() = definitions.values.toList()

  fun define(name: String, action: Action<in PropertyDefinition>) {
    definitions += (name to Actions.with(fork.project.objects.newInstance(PropertyDefinition::class.java, name), action))
  }

  fun define(definitions: Map<String, PropertyDefinition.() -> Unit>) {
    definitions.forEach { name, options -> define(name, Action { options(it) }) }
  }

  fun get(name: String): PropertyDefinition? = definitions[name]

  fun indexOf(name: String) = definitions.keys.indexOf(name)

}
