package com.neva.gradle.fork.gui

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.PropertyPrompt
import net.miginfocom.swing.MigLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class PropertyDialog(private val config: Config, private val prompts: List<PropertyPrompt>) {

  private val dialog = JDialog()

  private lateinit var fields: Map<PropertyPrompt, JTextField>

  private val close = JButton()

  private var canceled = false

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

      fields = prompts.fold(mutableMapOf(), { r, prompt ->
        val label = JLabel(if (prompt.required) "${prompt.name}*" else prompt.name)
        add(label, "align label")

        val field = when (prompt.type) {
          PropertyPrompt.Type.PASSWORD -> JPasswordField(prompt.valueOrDefault)
          else -> JTextField(prompt.valueOrDefault)
        }
        field.addActionListener { this@PropertyDialog.validate() }
        add(field, "width 300::, wrap")

        r[prompt] = field
        r
      })

      add(close.apply {
        text = "OK"
        addActionListener {
          if (valid) {
            dialog.dispose()
          } else {
            focusInvalidField()
          }
        }
      }, "span, south, wrap")

      addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
          e.window.dispose()
          canceled = true
        }
      })

      pack()
      centre()
      validate()
    }
  }

  fun prompt(): Map<String, String> {
    dialog.isVisible = true
    if (canceled) {
      throw ForkException("Fork canceled by interactive mode.")
    }

    return fields.entries.fold(mutableMapOf(), { r, e -> r[e.key.name] = e.value.text;r })
  }

  fun validate() {
    close.isEnabled = valid
  }

  val valid: Boolean
    get() = fields.all { isValidField(it.key, it.value) }

  fun isValidField(prompt: PropertyPrompt, field: JTextField): Boolean {
    return !prompt.required || field.text.isNotBlank()
  }

  fun focusInvalidField() {
    for ((prompt, field) in fields) {
      if (!isValidField(prompt, field)) {
        field.requestFocus()
        break
      }
    }
  }

  fun centre() {
    val dimension = Toolkit.getDefaultToolkit().screenSize
    val x = ((dimension.getWidth() - dialog.width) / 2).toInt()
    val y = ((dimension.getHeight() - dialog.height) / 2).toInt()

    dialog.setLocation(x, y)
  }

  companion object {

    fun prompt(config: Config, prompts: List<PropertyPrompt>): Map<String, String> {
      val laf = UIManager.getLookAndFeel()
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      val result = PropertyDialog(config, prompts).prompt()
      UIManager.setLookAndFeel(laf)

      return result
    }

  }

}
