package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.properties.Property
import com.neva.gradle.fork.config.properties.Validator
import java.awt.Color
import javax.swing.*

class PropertyDialogField(private val property: Property, private val propField: JComponent, private val validationMessageLabel: JLabel, private val dialog: JDialog) {

  init {
    setPropertyValue()
  }

  val name: String
    get() = property.name

  val value: String
    get() = property.value

  fun validateAndDisplayErrors(): Boolean {
    setPropertyValue()
    val result = property.validate()
    if (result.hasErrors()) {
      displayErrorState(result)
    } else {
      displayValidState()
    }
    dialog.pack()
    return result.hasErrors()
  }

  private fun setPropertyValue() {
    when (propField) {
      is JCheckBox -> property.value = propField.isSelected.toString()
      is JTextField -> property.value = propField.text
    }
  }

  private fun displayValidState() {
    validationMessageLabel.apply {
      foreground = null
      text = null
    }
    propField.background = null
  }

  private fun displayErrorState(result: Validator) {
    val errorMessage = "<html>${result.errors.joinToString(separator = "<br/>")}</html>"
    validationMessageLabel.apply {
      foreground = ERROR_TEXT_COLOR
      text = errorMessage
    }
    propField.background = ERROR_FIELD_COLOR
  }

  companion object {
    val ERROR_TEXT_COLOR: Color? = Color(255, 80, 80)
    val ERROR_FIELD_COLOR: Color? = Color(255, 221, 153)
  }
}
