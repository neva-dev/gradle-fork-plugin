package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.properties.Property
import com.neva.gradle.fork.config.properties.PropertyValidator
import java.awt.Color
import javax.swing.*

class PropertyDialogField(
  private val property: Property,
  private val dialog: JDialog,
  private val component: JComponent,
  private val validationMessageLabel: JLabel
) {

  init {
    sync()
  }

  val name: String
    get() = property.name

  val value: String
    get() = property.value

  fun control() {
    property.control()
  }

  fun render() {
    when (component) {
      is JCheckBox -> {
        if (component.isSelected != property.value.toBoolean()) {
          component.isSelected = property.value.toBoolean()
        }

      }
      is JComboBox<*> -> {
        if (component.selectedItem != property.value) {
          component.selectedItem = property.value
        }
      }
      is JTextField -> {
        if (component.text != property.value) {
          component.text = property.value
        }
      }
    }

    val result = property.validate()
    if (result.hasErrors()) {
      displayErrorState(result)
    } else {
      displayValidState()
    }
    component.isEnabled = property.enabled
    dialog.pack()
  }

  fun isInvalid() = property.validate().hasErrors()

  fun sync() {
    when (component) {
      is JCheckBox -> property.value = component.isSelected.toString()
      is JComboBox<*> -> property.value = component.selectedItem.toString()
      is JTextField -> property.value = component.text
    }
  }

  private fun displayValidState() {
    validationMessageLabel.apply {
      foreground = null
      text = null
    }
    component.background = null
  }

  private fun displayErrorState(result: PropertyValidator) {
    val errorMessage = "<html>${result.errors.joinToString(separator = "<br/>")}</html>"
    validationMessageLabel.apply {
      foreground = ERROR_TEXT_COLOR
      text = errorMessage
    }
    component.background = ERROR_FIELD_COLOR
  }

  companion object {

    val DESCRIPTION_TEXT_COLOR = Color(0, 0, 128)

    val ERROR_TEXT_COLOR = Color(255, 0, 0)

    val ERROR_FIELD_COLOR = Color(255, 240, 240)

  }
}
