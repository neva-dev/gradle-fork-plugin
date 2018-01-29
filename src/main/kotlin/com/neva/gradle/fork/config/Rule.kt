package com.neva.gradle.fork.config

interface Rule {

  fun validate()

  fun execute()

}
