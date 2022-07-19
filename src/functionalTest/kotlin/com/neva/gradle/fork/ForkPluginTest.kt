package com.neva.gradle.fork

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ForkPluginTest {

  private fun composeProject(name: String, composer: File.() -> Unit) = File("build/functionalTest/$name").apply {
    deleteRecursively()
    mkdirs()
    apply(composer)
  }

  @Suppress("SpreadOperator")
  private fun buildProject(projectDir: File, vararg args: String): BuildResult = GradleRunner.create()
    .forwardOutput()
    .withPluginClasspath()
    .withArguments(*args)
    .withProjectDir(projectDir)
    .build()

  @Test
  fun `can apply just fork`() {
    val projectDir = composeProject("fork") {
      resolve("settings.gradle.kts").writeText("")
      resolve("build.gradle.kts").writeText(
        """
                  plugins {
                      id("com.neva.fork")
                  }
                  """.trimIndent()
      )
    }
    val buildResult = buildProject(projectDir, "tasks")
    assertEquals(TaskOutcome.SUCCESS, buildResult.task(":tasks")?.outcome)
  }

  @Test
  fun `can apply just props`() {
    val projectDir = composeProject("props") {
      resolve("settings.gradle.kts").writeText("")
      resolve("build.gradle.kts").writeText(
        """
                  plugins {
                      id("com.neva.fork.props")
                  }
                  """.trimIndent()
      )
    }
    val buildResult = buildProject(projectDir, "tasks")
    assertEquals(TaskOutcome.SUCCESS, buildResult.task(":tasks")?.outcome)
  }
}
