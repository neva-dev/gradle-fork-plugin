# Gradle Fork Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/neva-dev/gradle-fork-plugin.svg?label=License)](http://www.apache.org/licenses/)

## Description

Project generator based on live archetypes (example projects).

Assumptions:

  * instead of creating a virtual project aka Maven Archetype with placeholders, plugin allows to treat any existing project like a base for a new project.
  * maintenance of real / working example projects is easier than maintaining archetypes (there is no need to regenerate project every time to prove that archetype is working properly).
  * it is easier to copy rich example project and remove redundant things than creating and assembling project from the scratch.
  * from business perspective, plugin allows to perform rebranding at code level (perform massive renaming, repackaging).

## Usage

```groovy
buildscript {
  repositories {
      jcenter()
      maven { url  "https://dl.bintray.com/neva-dev/maven-public" }
  }
  dependencies {
      classpath 'com.neva.gradle:fork-plugin:1.0.0-beta2'
  }
}

apply plugin: 'com.neva.fork'

fork {
    config {
        cloneFiles()
        moveFiles([
                "/example": "/{{projectName}}"
        ])
        replaceContents([
                "com.company.app.example": "{{package}}",
                "example": "{{projectName}}",
                "Example": "{{projectLabel}}"
        ])
        copyTemplateFile("gradle.properties")
    }
}
```

Fork configuration above will:

* copy all project files respecting filtering defined in *.gitignore* files,
* rename directories using rules with properties injecting,
* replace contents using rules with properties injecting.

To execute fork configuration, run command:

```bash
sh gradlew fork
```

Properties can be provided by (order makes precedence):
 
1. File which path could be specified as command line parameter:

    ```bash
    sh gradlew fork -Pfork.properties=fork.properties
    ```
    
    Such file should be in format:
    
    ```bash
    targetPath=../sample
    projectName=sample
    projectLabel=Sample
    package=com.neva.app.sample
    ```
  
2. Each property defined separately as command line parameter:

    ```bash
    sh gradlew fork -PprojectName=sample -PprojectLabel=Sample -PtargetPath=../sample -Ppackage=com.neva.app.sample
    ```

3. GUI / properties dialog

    ![Props Dialog](docs/props-dialog.png)
    
    This dialog is being displayed always when there are properties which have no value provided.
    
    To enforce displaying (or not) dialog above regardless values provided, use command line parameter:
    
    ```bash
    sh gradlew fork -Pfork.interactive=true
    ```
  
4. Mixed approach.

As a fork result, there will be a cloned project with correctly changed directory names, with replaced project name and label in text files (all stuff being previously performed manually).

<pre>
Cloning files from C:\Users\krystian.panek\Projects\example to ..\sample
Copying file from C:\Users\krystian.panek\Projects\example\.editorconfig to ..\sample\.editorconfig
...
Moving file from C:\Users\krystian.panek\Projects\example\apps\example\content.xml to ..\sample\apps\sample\content.xml
...
Replacing 'Example' with 'Sample' in file C:\Users\krystian.panek\Projects\sample\app\build.gradle
Replacing 'com.company.aem.example' with 'com.neva.aem.sample' in file C:\Users\krystian.panek\Projects\sample\app\common\build.gradle
Replacing 'example' with 'sample' in file C:\Users\krystian.panek\Projects\sample\app\common\src\main\content\META-INF\vault\filter.xml
...
Copying file from C:\Users\krystian.panek\Projects\example\gradle\fork\gradle.properties to ..\sample\gradle.properties
Expanding properties in file ..\sample\gradle.properties
</pre>
