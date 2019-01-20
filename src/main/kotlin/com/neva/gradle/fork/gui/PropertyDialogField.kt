package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.properties.Property
import com.neva.gradle.fork.config.properties.PropertyValidator
import java.awt.Color
import javax.swing.*

class PropertyDialogField(
  private val property: Property,
  private val dialog: JDialog,
  private val propField: JComponent,
  private val label: JLabel,
  private val validationMessageLabel: JLabel,
  private val descriptionLabel: JLabel
) {

  init {
    assignValue()
  }

  val name: String
    get() = property.name

  val value: String
    get() = property.value

  fun control() = property.control()
  
  fun toggle(flag: Boolean) {
    if (flag) {
      propField.isVisible = true
      label.isVisible = true
      validationMessageLabel.isVisible = true
      descriptionLabel.isVisible = true
    } else {
      propField.isVisible = false
      label.isVisible = false
      validationMessageLabel.isVisible = false
      descriptionLabel.isVisible = false
    }
  }

  fun validateAndDisplayErrors() {
    assignValue()
    val result = property.validate()
    if (result.hasErrors()) {
      displayErrorState(result)
    } else {
      displayValidState()
    }
    toggle(property.enabled)
    dialog.pack()
  }

  fun isInvalid() = property.validate().hasErrors()

  fun assignValue() {
    when (propField) {
      is JCheckBox -> property.value = propField.isSelected.toString()
      is JComboBox<*> -> property.value = propField.selectedItem.toString()
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

  private fun displayErrorState(result: PropertyValidator) {
    val errorMessage = "<html>${result.errors.joinToString(separator = "<br/>")}</html>"
    validationMessageLabel.apply {
      foreground = ERROR_TEXT_COLOR
      text = errorMessage
    }
    propField.background = ERROR_FIELD_COLOR
  }

  companion object {

    val DESCRIPTION_TEXT_COLOR = Color(0, 0, 128)

    val ERROR_TEXT_COLOR = Color(255, 0, 0)

    val ERROR_FIELD_COLOR = Color(255, 240, 240)

  }
}
