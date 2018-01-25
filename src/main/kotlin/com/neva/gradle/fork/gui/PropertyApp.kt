package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.Config
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App
import tornadofx.importStylesheet

class PropertyApp : App(PropertyScreen::class) {
  val loginController: PropertyController by inject()


  override fun start(stage: Stage) {
    importStylesheet(PropertyStyles::class)
    super.start(stage)
    loginController.init()


  }

  companion object {
    lateinit var forkConfig: Config

    @Synchronized
    fun launch(config: Config) {
      PropertyApp.forkConfig = config
      Application.launch(PropertyApp::class.java)
      PropertyApp.forkConfig
    }
  }
}

