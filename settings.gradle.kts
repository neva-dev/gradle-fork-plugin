rootProject.name = "fork-plugin"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "${extra["kotlin.version"]}"
        id("io.gitlab.arturbosch.detekt") version "${extra["detekt.version"]}"
        id("org.jetbrains.dokka") version "${extra["dokka.version"]}"
        id("com.gradle.plugin-publish") version "0.10.1"
        id("com.jfrog.bintray") version "1.8.4"
        id("net.researchgate.release") version "2.6.0"
        id("com.github.breadmoirai.github-release") version "2.2.9"
    }
}
