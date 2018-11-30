import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.3.10"
    id("com.jfrog.bintray") version "1.8.4"
    id("maven-publish")
}

group = "com.neva.gradle"
version = "1.0.8"
description = "Gradle Fork Plugin"
defaultTasks = listOf("clean", "publishToMavenLocal")

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://dl.bintray.com/neva-dev/maven-public") }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.10")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("commons-io:commons-io:2.4")
    implementation("commons-validator:commons-validator:1.6")
    implementation("com.neva.commons:gitignore-file-filter:1.0.0")
    implementation("com.miglayout:miglayout:3.7.4")
    implementation("io.pebbletemplates:pebble:3.0.4")

    testImplementation("junit:junit:4.12")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

gradlePlugin {
    plugins {
        create("fork") {
            id = "com.neva.fork"
            implementationClass = "com.neva.gradle.fork.ForkPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

bintray {
    user = (project.findProperty("bintray.user") ?: System.getenv("BINTRAY_USER"))?.toString()
    key = (project.findProperty("bintray.key") ?: System.getenv("BINTRAY_KEY"))?.toString()
    setPublications("mavenJava")
    with(pkg) {
        repo = "maven-public"
        name = "gradle-fork-plugin"
        userOrg = "cognifide"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/neva-dev/gradle-fork-plugin.git"
        setLabels("gradle", "archetype", "template")
        with(version) {
            name = project.version.toString()
            desc = "${project.description} ${project.version}"
            vcsTag = project.version.toString()
        }
    }
    publish = (project.findProperty("bintray.publish") ?: "true").toString().toBoolean()
    override = (project.findProperty("bintray.override") ?: "false").toString().toBoolean()
}
