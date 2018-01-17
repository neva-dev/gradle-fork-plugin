package com.neva.gradle.fork.gui

import groovy.swing.SwingBuilder

class PropsDialog {

    Map<String, String> prompt(Map<String, String> props) {
        def result = [:]
        result.putAll(props)

        new SwingBuilder().edt {
            dialog(
                    modal: true,
                    title: 'Fork properties',
                    alwaysOnTop: true,
                    resizable: false,
                    locationRelativeTo: null,
                    pack: true,
                    show: true
            ) {
                vbox {
                    props.each { k, v ->
                        label(text: k)
                        input = textField(text: v, actionPerformed: { result[k] = input.text })
                    }

                    button(defaultButton: true, text: 'OK', actionPerformed: {
                        dispose()
                    })
                }
            }
        }

        return result
    }
}
