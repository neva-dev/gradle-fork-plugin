package com.neva.gradle.fork.gui

import groovy.swing.SwingBuilder

class PropertiesDialog {

    Map<String, String> prompt() {
        def pass = ''
        
        new SwingBuilder().edt {
            dialog(modal: true, // Otherwise the build will continue running before you closed the dialog
                    title: 'Enter password', // Dialog title
                    alwaysOnTop: true, // pretty much what the name says
                    resizable: false, // Don't allow the user to resize the dialog
                    locationRelativeTo: null, // Place dialog in center of the screen
                    pack: true, // We need to pack the dialog (so it will take the size of it's children)
                    show: true // Let's show it
            ) {
                vbox { // Put everything below each other
                    label(text: "Please enter key passphrase:")
                    input = passwordField()
                    button(defaultButton: true, text: 'OK', actionPerformed: {
                        pass = input.password; // Set pass variable to value of input field
                        dispose(); // Close dialog
                    })
                } // vbox end
            } // dialog end
        } // edt end
        
        return ["password": pass]
    }
}
