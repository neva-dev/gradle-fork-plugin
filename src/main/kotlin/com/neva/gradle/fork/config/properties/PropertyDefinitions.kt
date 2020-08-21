package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkExtension
import org.gradle.api.Action

class PropertyDefinitions(val fork: ForkExtension) {

  private var definitions = mutableMapOf<String, PropertyDefinition>()

  val all: List<PropertyDefinition>
    get() = definitions.values.toList()

  fun define(name: String, action: Action<in PropertyDefinition>): PropertyDefinition {
    return fork.project.objects.newInstance(PropertyDefinition::class.java, name).apply {
      action.execute(this)
      definitions[name] = this
    }
  }

  fun define(definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return definitions.map { (name, options) -> define(name) { options(it) } }
  }

  fun define(commonDefinition: Action<in PropertyDefinition>, definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return define(definitions).apply { forEach { commonDefinition.execute(it) } }
  }

  fun define(group: String, definitions: Map<String, PropertyDefinition.() -> Unit>): List<PropertyDefinition> {
    return define({ it.group = group }, definitions)
  }

  fun group(group: String, action: Action<in PropertyDefinitions>) {
    val previousDefinitions = this.definitions
    this.definitions = mutableMapOf()
    action.execute(this)
    this.definitions = (previousDefinitions + this.definitions.onEach { it.value.group = group }).toMutableMap()
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
