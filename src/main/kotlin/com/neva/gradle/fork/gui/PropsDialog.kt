package com.neva.gradle.fork.gui

import com.neva.gradle.fork.ForkException
import net.miginfocom.swing.MigLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


class PropsDialog(defaults: Map<String, String>) {

  private val fields = mutableMapOf<String, JTextField>()

  private val dialog = JDialog()

  private val close = JButton()

  private var canceled = false

  init {
    dialog.apply {
      title = "Fork properties"
      layout = MigLayout(
        "insets 10 10 10 10",
        "[fill,grow][fill,grow]",
        "[fill,grow]"
      )
      isAlwaysOnTop = true
      isModal = true
      isResizable = false

      defaults.forEach { prop, defaultValue ->
        val field = JTextField(defaultValue)
        field.addActionListener { this@PropsDialog.validate() }
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

    return fields.mapValues { it.value.text }
  }

  fun validate() {
    close.isEnabled = valid
  }

  val valid: Boolean
    get() = fields.all { !it.value.text.isNullOrBlank() }

  fun centre() {
    val dimension = Toolkit.getDefaultToolkit().screenSize
    val x = ((dimension.getWidth() - dialog.width) / 2).toInt()
    val y = ((dimension.getHeight() - dialog.height) / 2).toInt()

    dialog.setLocation(x, y)
  }

  companion object {

    fun prompt(defaults: Map<String, String>): Map<String, String> {
      val laf = UIManager.getLookAndFeel()
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      val result = PropsDialog(defaults).prompt()
      UIManager.setLookAndFeel(laf)

      return result
    }

  }

}
