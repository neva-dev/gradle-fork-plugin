package com.neva.gradle.fork.gui

import java.awt.event.FocusEvent
import java.awt.event.FocusListener as Base

abstract class FocusListener : Base {

  override fun focusLost(e: FocusEvent) {
    // intentionally empty
  }

  override fun focusGained(e: FocusEvent) {
    // intentionally empty
  }

}
