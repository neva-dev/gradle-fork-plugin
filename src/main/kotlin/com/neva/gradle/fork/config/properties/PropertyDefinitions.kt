package com.neva.gradle.fork.config.properties

import com.neva.gradle.fork.ForkExtension
import org.gradle.api.Action
import org.gradle.internal.Actions

class PropertyDefinitions(val fork: ForkExtension) {

  private val definitions = mutableMapOf<String, PropertyDefinition>()

  fun define(name: String, action: Action<in PropertyDefinition>) {
    definitions += (name to Actions.with(fork.project.objects.newInstance(PropertyDefinition::class.java, name), action))
  }

  fun get(name: String): PropertyDefinition? {
    return definitions[name]
  }

}
