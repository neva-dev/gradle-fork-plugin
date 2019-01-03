package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.properties.Property
import com.neva.gradle.fork.config.properties.Validator
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JTextField

class PropertyDialogField(private val property: Property, private val propField: JTextField, private val validationMessageLabel: JLabel) {

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
    return result.hasErrors()
  }

  private fun setPropertyValue() {
    property.value = propField.text
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
