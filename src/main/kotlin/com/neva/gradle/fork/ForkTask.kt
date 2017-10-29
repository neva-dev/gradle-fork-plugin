package com.neva.gradle.fork

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ForkTask : DefaultTask() {

    companion object {
        val NAME = "fork"
    }

    @TaskAction
    fun fork() {
        logger.info("Forking executed")
    }

}