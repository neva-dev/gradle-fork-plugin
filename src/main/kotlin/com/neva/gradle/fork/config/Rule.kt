package com.neva.gradle.fork.config

/**
 * Represents an action being a part of configuration.
 */
interface Rule {

  fun validate()

  fun execute()

}
