package com.neva.gradle.fork.gui

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class PropertyStyles : Stylesheet() {

  companion object {
    val loginScreen by cssclass()
  }

  init {
    select(loginScreen) {
      padding = box(15.px)
      vgap = 7.px
      hgap = 10.px
    }
  }

}
