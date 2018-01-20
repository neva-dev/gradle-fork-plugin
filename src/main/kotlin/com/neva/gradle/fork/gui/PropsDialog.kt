package com.neva.gradle.fork.gui

import com.neva.gradle.fork.ForkException
import net.miginfocom.swing.MigLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JTextField

class PropsDialog(defaults: Map<String, String>) {

  private val fields = mutableMapOf<String, JTextField>()

  private val dialog = JDialog()

  private val close = JButton()

  init {
    dialog.apply {
      title = "Fork properties"
      layout = MigLayout(
        "insets 4 4 4 4",
        "[fill,grow][fill,grow]",
        "[fill,grow]"
      )
      isAlwaysOnTop = true
      isModal = true
      isResizable = false
      setLocationRelativeTo(null)

      defaults.forEach { prop, defaultValue ->
        val field = JTextField(defaultValue)
        field.addActionListener { close.isEnabled = valid }
        fields[prop] = field

        add(JLabel(prop), "align label")
        add(field, "width 300::, wrap")
      }

      add(close.apply {
        text = "OK"
        addActionListener {
          if (valid) {
            dialog.dispose()
          }
        }
      }, "span, south, wrap")

      addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
          e.window.dispose()
          if (!valid) {
            throw ForkException("Fork properties are not specified.")
          }
        }
      })

      pack()
    }
  }

  fun prompt(): Map<String, String> {
    dialog.isVisible = true
    return fields.mapValues { it.value.text }
  }

  val valid: Boolean
    get() = fields.all { !it.value.text.isNullOrBlank() }

}
