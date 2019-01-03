package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.properties.Property
import com.neva.gradle.fork.config.properties.ValidatorErrors
import java.awt.Color
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JTextField

class PropertyDialogField(private val property: Property, private val propField: JTextField, private val validationMessageLabel: JLabel, private val dialog: JDialog) {

  val name: String
    get() = property.name

  val textValue: String
    get() = propField.text

  fun validateAndDisplayErrors(): Boolean {
    val result = property.validate(textValue)
    if (result.hasErrors()) {
      displayErrorState(result)
    } else {
      displayValidState()
    }
    return result.hasErrors()
  }

  private fun displayValidState() {
    validationMessageLabel.apply {
      foreground = null
      text = null
    }
    propField.background = null
    dialog.pack()
  }

  private fun displayErrorState(result: ValidatorErrors) {
    val errorMessage = "<html>${result.errors.joinToString(separator = "<br/>")}</html>"
    validationMessageLabel.apply {
      foreground = ERROR_TEXT_COLOR
      text = errorMessage
    }
    propField.background = ERROR_FIELD_COLOR
    dialog.pack()
  }

  companion object {
    val ERROR_TEXT_COLOR: Color? = Color(255, 80, 80)
    val ERROR_FIELD_COLOR: Color? = Color(255, 221, 153)
  }
}
