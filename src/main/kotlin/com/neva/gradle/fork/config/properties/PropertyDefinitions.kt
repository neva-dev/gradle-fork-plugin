package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkExtension
import org.gradle.api.Action
import org.gradle.internal.Actions

class PropertyDefinitions(val fork: ForkExtension) {

  private val definitions = mutableMapOf<String, PropertyDefinition>()

  val all: List<PropertyDefinition>
    get() = definitions.values.toList()

  fun define(name: String, action: Action<in PropertyDefinition>): PropertyDefinition {
    val definition = Actions.with(fork.project.objects.newInstance(PropertyDefinition::class.java, name), action)
    definitions += name to definition
    return definition
  }

  fun define(definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return definitions.map { (name, options) -> define(name, Action { options(it) }) }
  }

  fun define(group: String, definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return define(Action { it.group = group }, definitions)
  }

  fun define(commonDefinition: Action<in PropertyDefinition>, definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return define(definitions).apply { forEach { commonDefinition.execute(it) } }
  }

  fun get(name: String): PropertyDefinition? = definitions[name]

  fun indexOf(name: String) = definitions.keys.indexOf(name)

  // utility functions for e.g default values

  fun pathTo(path: String, normalize: Boolean = true): String {
    return fork.project.rootProject.file(path).absolutePath.run {
      if (normalize) this.replace("\\", "/") else this
    }
  }
}
