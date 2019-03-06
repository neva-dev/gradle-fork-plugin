package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.properties.PropertyType
import net.miginfocom.swing.MigLayout
import java.awt.Toolkit
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent


class PropertyDialog(private val config: Config) {

  private val dialog = JDialog().apply {
    title = "Properties for configuration '${config.name}'"
    layout = MigLayout(
      "insets 10 10 10 10",
      "[fill,grow][fill,grow]",
      "[fill,grow]"
    )
    isAlwaysOnTop = true
    isModal = true
    isResizable = false

    addWindowListener(object : WindowAdapter() {
      override fun windowClosing(e: WindowEvent) {
        e.window.dispose()
        cancelled = true
      }
    })
  }

  private var pathButton = JButton().apply {
    if (config.definedProperties.any { it.type == PropertyType.PATH || it.type == PropertyType.URI }) {
      text = "Pick a path"
      addActionListener {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
          fieldFocused!!.document.insertString(
            fieldFocused!!.caretPosition,
            fileChooser.selectedFile.absolutePath.replace("\\", "/"),
            null
          )
        }
      }

      dialog.add(this, "span, wrap")
    }
  }

  @Suppress("unchecked_cast")
  private var fields: List<PropertyDialogField> = config.definedProperties.map { property ->
    val label = JLabel(property.label)
    dialog.add(label, "align label")

    val field: JComponent = when (property.type) {
      PropertyType.PASSWORD -> JPasswordField(property.value)
      PropertyType.CHECKBOX -> JCheckBox("", property.value.toBoolean())
      PropertyType.SELECT -> JComboBox((property.options as List<String>).toTypedArray()).apply { setSelectedItem(property.value) }
      else -> JTextField(property.value)
    }

    when (field) {
      is JTextField -> {
        field.document.addDocumentListener(object : DocumentListener() {
          override fun change(e: DocumentEvent) {
            this@PropertyDialog.update()
          }
        })
        field.addFocusListener(object : FocusListener() {
          override fun focusGained(e: FocusEvent) {
            fieldFocused = field
            this@PropertyDialog.update()
          }
        })
      }
      is JCheckBox -> {
        field.addItemListener(ItemListener { this@PropertyDialog.update() })
      }
      is JComboBox<*> -> {
        field.addActionListener(ActionListener { this@PropertyDialog.update() })
      }
    }

    dialog.add(field, "width 300::, wrap")

    val validationMessage = JLabel()
    dialog.add(validationMessage, "skip, wrap")

    if (property.description.isNotBlank()) {
      val descriptionHtml = "<html>${property.description.replace("\n", "<br/>")}</html>"
      val descriptionLabel = JLabel(descriptionHtml).apply {
        foreground = PropertyDialogField.DESCRIPTION_TEXT_COLOR
      }
      dialog.add(descriptionLabel, "skip, wrap")
    }

    PropertyDialogField(property, dialog, field, validationMessage)
  }

  private var closeButton = JButton().apply {
    text = "Execute"
    addActionListener {
      if (valid) {
        dialog.dispose()
      }
    }

    dialog.add(this, "span, south, wrap")
  }

  private val fileChooser = JFileChooser().apply {
    fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
  }

  private var fieldFocused: JTextField? = null

  var cancelled = false

  init {
    dialog.apply {
      pack()
      centre()
      update()
    }
  }

  val props: Map<String, String>
    get() = fields.fold(mutableMapOf()) { r, e -> r[e.name] = e.value;r }

  fun update() {
    val isFieldSelected = fieldFocused != null

    validateAllFields()
    closeButton.isEnabled = valid
    pathButton.isEnabled = isFieldSelected
    dialog.isVisible = true
  }

  private fun validateAllFields() {
    fields.forEach(PropertyDialogField::assignValue)
    fields.forEach(PropertyDialogField::control)
    fields.forEach(PropertyDialogField::validateAndDisplayErrors)
  }

  private val valid: Boolean
    get() = fields.none(PropertyDialogField::isInvalid)

  fun centre() {
    val dimension = Toolkit.getDefaultToolkit().screenSize
    val x = ((dimension.getWidth() - dialog.width) / 2).toInt()
    val y = ((dimension.getHeight() - dialog.height) / 2).toInt()

    dialog.setLocation(x, y)
  }

  companion object {

    fun make(config: Config): PropertyDialog {
      val laf = UIManager.getLookAndFeel()
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      val dialog = PropertyDialog(config)
      UIManager.setLookAndFeel(laf)
      return dialog
    }
  }
}
