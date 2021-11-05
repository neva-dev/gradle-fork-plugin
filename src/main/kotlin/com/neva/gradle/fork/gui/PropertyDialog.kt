package com.neva.gradle.fork.gui

import com.neva.gradle.fork.ForkException
import com.neva.gradle.fork.config.Config
import com.neva.gradle.fork.config.properties.PropertyType
import net.miginfocom.swing.MigLayout
import java.awt.Container
import java.awt.HeadlessException
import java.awt.Toolkit
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent

class PropertyDialog(private val config: Config) {

  private val logger = config.project.logger

  private val fileChooser by lazy {
    JFileChooser().apply {
      fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
      isFileHidingEnabled = false
    }
  }

  private val dialog = JDialog().apply {
    title = config.description.get()
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

  private val tabPane by lazy {
    JTabbedPane().apply {
      dialog.add(this, "span, wrap")
    }
  }

  private val tabs = config.definedProperties.fold(mutableMapOf<String, JPanel>(), { result, property ->
    property.definition.group?.let { group -> result.computeIfAbsent(group) { addTab(group) } }
    result
  })

  private fun addTab(group: String) = JPanel().apply {
      layout = MigLayout(
        "insets 10 10 10 10",
        "[fill,grow][fill,grow]",
        "[fill,fill]"
      )

      tabPane.addTab(group, this)
    }

  @Suppress("unchecked_cast")
  private val fields: List<PropertyDialogField> = config.definedProperties.map { property ->
    val label = JLabel(property.label)
    val container: Container = tabs[property.definition.group] ?: dialog

    container.add(label, "align label")

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
            this@PropertyDialog.render()
          }
        })
        field.addFocusListener(object : FocusListener() {
          override fun focusGained(e: FocusEvent) {
            fieldFocused = field
            this@PropertyDialog.render()
          }
        })
      }
      is JCheckBox -> {
        field.addItemListener(ItemListener { this@PropertyDialog.render() })
      }
      is JComboBox<*> -> {
        field.addActionListener(ActionListener { this@PropertyDialog.render() })
      }
    }

    container.add(field, "width 300::, wrap")

    val validationMessage = JLabel()
    container.add(validationMessage, "skip, wrap")

    if (property.description.isNotBlank()) {
      val descriptionHtml = "<html>${property.description.replace("\n", "<br/>")}</html>"
      val descriptionLabel = JLabel(descriptionHtml).apply {
        foreground = PropertyDialogField.DESCRIPTION_TEXT_COLOR
      }
      container.add(descriptionLabel, "skip, wrap")
    }

    PropertyDialogField(property, dialog, field, validationMessage)
  }

  @Suppress("TooGenericExceptionCaught")
  private val pathButton = JButton().apply {
    text = "Pick a path"
    addActionListener {
      try {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
          fieldFocused!!.document.insertString(
            fieldFocused!!.caretPosition,
            fileChooser.selectedFile.absolutePath.replace("\\", "/"),
            null
          )
        }
      } catch (e: Exception) {
        logger.debug("Cannot show file chooser dialog!", e)
      }
    }

    dialog.add(this)
  }

  private val closeButton = JButton().apply {
    text = "Execute"
    addActionListener {
      if (valid) {
        dialog.dispose()
      }
    }

    dialog.add(this, "span, wrap")
  }

  private var fieldFocused: JTextField? = null

  var cancelled = false

  init {
    dialog.apply {
      pack()
      centre()
      render()
    }
  }

  val props: Map<String, String>
    get() = fields.fold(mutableMapOf()) { r, e -> r[e.name] = e.value; r }

  fun render() {
    fields.forEach { it.sync() }
    fields.forEach { it.control() }
    SwingUtilities.invokeLater {
      fields.forEach { it.render() }
    }

    closeButton.isEnabled = valid
    pathButton.isEnabled = fieldFocused != null && enabledPathProperty
    dialog.isVisible = true
  }

  private val enabledPathProperty get() = config.definedProperties.any {
    it.enabled && (it.type == PropertyType.PATH || it.type == PropertyType.URI)
  }

  private val valid: Boolean get() = fields.none(PropertyDialogField::isInvalid)

  fun centre() {
    val dimension = Toolkit.getDefaultToolkit().screenSize
    val x = ((dimension.getWidth() - dialog.width) / 2).toInt()
    val y = ((dimension.getHeight() - dialog.height) / 2).toInt()

    dialog.setLocation(x, y)
  }

  companion object {

    private const val TROUBLESHOOTING = "Please run 'sh gradlew --stop' then try again.\n" +
      "Ultimately run command with '--no-daemon' option."

    @Suppress("TooGenericExceptionCaught")
    fun make(config: Config): PropertyDialog = try {
      val laf = UIManager.getLookAndFeel()
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      val dialog = PropertyDialog(config)
      UIManager.setLookAndFeel(laf)
      dialog
    } catch (e: HeadlessException) {
      throw ForkException("Fork properties GUI dialog cannot be opened in headless mode!\n$TROUBLESHOOTING")
    } catch (e: Exception) {
      throw ForkException("Fork properties GUI dialog cannot be opened!\n$TROUBLESHOOTING", e)
    }
  }
}
