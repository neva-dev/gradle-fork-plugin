package com.neva.gradle.fork

import org.gradle.api.GradleException

class ForkException : GradleException {

  constructor(message: String) : super(message)

}
