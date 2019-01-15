package com.neva.gradle.fork.config

import com.neva.gradle.fork.ForkExtension

class InPlaceConfig(forkExtension: ForkExtension, name: String) : Config(forkExtension, name) {

  override val sourcePath: String by lazy { project.projectDir.absolutePath }

  override val targetPath: String by lazy { sourcePath }

}
