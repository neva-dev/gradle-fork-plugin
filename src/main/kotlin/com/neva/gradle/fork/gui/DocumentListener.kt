package com.neva.gradle.fork.gui

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener as Base

abstract class DocumentListener : Base {

  override fun changedUpdate(e: DocumentEvent) {
    change(e)
  }

  override fun insertUpdate(e: DocumentEvent) {
    change(e)
  }

  override fun removeUpdate(e: DocumentEvent) {
    change(e)
  }

  abstract fun change(e: DocumentEvent)
}
