package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.properties.PropertyPrompt
import net.miginfocom.swing.MigLayout
import java.awt.Toolkit
import java.awt.event.FocusEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.DocumentEvent

class PropertyDialog(private val config: Config) {

  private val dialog = JDialog().apply {
    title = "Fork properties for config '${config.name}'"
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

  private var fields: List<PropertyDialogField> = config.properties
    .map { property ->
      val label = JLabel(property.label)
      dialog.add(label, "align label")

      val field = when (property.type) {
        PropertyPrompt.Type.PASSWORD -> JPasswordField(property.value)
        else -> JTextField(property.value)
      }
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
      dialog.add(field, "width 300::, wrap")

      val validationMessage = JLabel()
      dialog.add(validationMessage, "skip, wrap")

      PropertyDialogField(property, field, validationMessage)
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

  private val fileChooser = JFileChooser()

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
    closeButton.isEnabled = valid
    pathButton.isEnabled = isFieldSelected
    dialog.isVisible = true
    dialog.pack()
  }

  private val valid: Boolean
    get() = fields.filter(PropertyDialogField::validateAndDisplayErrors).isEmpty()

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

