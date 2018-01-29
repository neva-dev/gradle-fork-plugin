package com.neva.gradle.fork.gui

import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.PropertyPrompt
import net.miginfocom.swing.MigLayout
import java.awt.Toolkit
import java.awt.event.FocusEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.DocumentEvent

class PropertyDialog(private val config: Config) {

  private val dialog = JDialog()

  private val fileChooser = JFileChooser()

  private lateinit var fields: Map<PropertyPrompt, JTextField>

  private var fieldFocused: JTextField? = null

  private lateinit var pathButton: JButton

  private lateinit var closeButton: JButton

  var cancelled = false

  init {
    dialog.apply {
      title = "Fork properties for config '${config.name}'"
      layout = MigLayout(
        "insets 10 10 10 10",
        "[fill,grow][fill,grow]",
        "[fill,grow]"
      )
      isAlwaysOnTop = true
      isModal = true
      isResizable = false

      pathButton = JButton("Pick a path")
      add(pathButton.apply {
        addActionListener {
          if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fieldFocused!!.document.insertString(
              fieldFocused!!.caretPosition,
              fileChooser.selectedFile.absolutePath.replace("\\", "/"),
              null
            )
          }
        }
      }, "span, wrap")

      fields = config.prompts.values.fold(mutableMapOf(), { r, prompt ->
        val label = JLabel(if (prompt.required) "${prompt.label}*" else prompt.label)
        add(label, "align label")

        val field = when (prompt.type) {
          PropertyPrompt.Type.PASSWORD -> JPasswordField(prompt.valueOrDefault)
          else -> JTextField(prompt.valueOrDefault)
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
        add(field, "width 300::, wrap")

        r[prompt] = field
        r
      })

      closeButton = JButton()
      add(closeButton.apply {
        text = "Execute"
        addActionListener {
          if (valid) {
            dialog.dispose()
          }
        }
      }, "span, south, wrap")

      addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
          e.window.dispose()
          cancelled = true
        }
      })

      pack()
      centre()
      update()
    }
  }

  val props : Map<String, String>
    get() = fields.entries.fold(mutableMapOf(), { r, e -> r[e.key.name] = e.value.text;r })

  fun update() {
    closeButton.isEnabled = valid
    pathButton.isEnabled = fieldFocused != null
    dialog.isVisible = true
  }

  val valid: Boolean
    get() = fields.all { isValidField(it.key, it.value) }

  fun isValidField(prompt: PropertyPrompt, field: JTextField): Boolean {
    return !prompt.required || field.text.isNotBlank()
  }

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
