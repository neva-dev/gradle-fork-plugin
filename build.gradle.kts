import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("net.researchgate.release")
    id("com.gradle.plugin-publish")
    id("maven-publish")
    id("com.github.breadmoirai.github-release")
}

group = "com.neva.gradle"
description = "Gradle Fork Plugin"
defaultTasks("clean", "publishToMavenLocal")

val functionalTestSourceSet = sourceSets.create("functionalTest")
gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    dependsOn("detektFunctionalTest")
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.20")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("commons-io:commons-io:2.6")
    implementation("commons-validator:commons-validator:1.6")
    implementation("commons-codec:commons-codec:1.15")

    implementation("com.neva.commons:gitignore-file-filter:1.0.0")
    implementation("com.miglayout:miglayout:3.7.4")
    implementation("io.pebbletemplates:pebble:3.1.2")
    implementation("nu.studer:java-ordered-properties:1.0.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:${properties["detekt.version"]}")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile>().configureEach{
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
    withType<Test>().configureEach {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
    named("build") { 
        dependsOn("sourcesJar")
    }

    named("publishToMavenLocal") { 
        dependsOn("sourcesJar")
    }
    named("afterReleaseBuild") {
        dependsOn("publishPlugins")
    }
    register("fullRelease") {
        dependsOn("release", "githubRelease")
    }
    named("githubRelease") {
        mustRunAfter("release")
    }
    check {
        dependsOn(functionalTest)
    }
}

detekt {
    config.from(file("detekt.yml"))
    parallel = true
    autoCorrect = true
    failFast = true
}

gradlePlugin {
    plugins {
        create("fork") {
            id = "com.neva.fork"
            implementationClass = "com.neva.gradle.fork.ForkPlugin"
            displayName = "Fork Plugin"
            description = "Project generator based on live archetypes (example projects) & interactive 'gradle.properties' file generator."
        }
        create("props") {
            id = "com.neva.fork.props"
            implementationClass = "com.neva.gradle.fork.PropsPlugin"
            displayName = "Fork Properties Plugin"
            description = "Extension to Fork Plugin for reading encrypted properties like passwords."
        }
    }
}

pluginBundle {
    website = "https://github.com/neva-dev/gradle-fork-plugin"
    vcsUrl = "https://github.com/neva-dev/gradle-fork-plugin.git"
    description = "Gradle Fork Plugin"
    tags = listOf("archetype", "template", "properties", "password-encryption", "maven-archetype", "gui")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

githubRelease {
    owner("neva-dev")
    repo("gradle-fork-plugin")
    token((project.findProperty("github.token") ?: "").toString())
    tagName(project.version.toString())
    releaseName(project.version.toString())
    releaseAssets(project.fileTree("build/libs") { include("**/${project.name}-${project.version}*.jar") })
    draft((project.findProperty("github.draft") ?: "false").toString().toBoolean())
    prerelease((project.findProperty("github.prerelease") ?: "false").toString().toBoolean())
    overwrite((project.findProperty("github.override") ?: "false").toString().toBoolean())

    body { """
        |# What's new
        |
        |TBD
        |
        |# Upgrade notes
        |
        |Nothing to do.
        |
        |# Contributions
        |
        |None.
        """.trimMargin()
    }
}
