package com.neva.gradle.fork

import org.gradle.api.GradleException

open class ForkException : GradleException {

  constructor(message: String) : super(message)

}
