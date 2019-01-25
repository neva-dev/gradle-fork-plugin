![Neva logo](docs/neva-logo.png)

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/neva-dev/gradle-fork-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![GitHub stars](https://img.shields.io/github/stars/neva-dev/gradle-fork-plugin.svg)](https://github.com/neva-dev/gradle-fork-plugin/stargazers)

# Gradle Fork Plugin

## Description

**Project generator based on live archetypes** (example projects).

**Interactive gradle.properties file generator** (user-friendly / by GUI dialog).

- - -

Newcomers of Gradle Build System very often complain about that in Gradle there is no Maven's archetype like mechanism OOTB. This plugin tries to fill that gap.

**Assumptions**

  * Instead of creating a virtual project aka Maven Archetype with placeholders, plugin allows to treat any existing project like a base for a new project.
  * It is easier to copy rich example project and remove redundant things than creating project from archetype and looking for missing things.
  * From business perspective, plugin allows to automate rebranding at code level (perform massive renaming, repackaging).
  * Maintenance of real / working example projects is probably easier than maintaining archetypes (there is no need to regenerate project every time to prove that archetype is working properly).

Plugin is also useful for **generating** *gradle.properties* file in a **user-friendly way.

## Table of Contents

* [Usage](#usage)
   * [Sample build script](#sample-build-script)
   * [Defining and executing configurations](#defining-and-executing-configurations)
   * [Providing properties](#providing-properties)
   * [Defining project properties](#defining-project-properties)
   * [Password encryption](#password-encryption)
   * [Sample output](#sample-output)
* [License](#license)

## Usage

### Sample build script

```groovy
buildscript {
  repositories {
      jcenter()
      maven { url  "https://dl.bintray.com/neva-dev/maven-public" }
  }
  dependencies {
      classpath 'com.neva.gradle:fork-plugin:3.0.4'
  }
}

apply plugin: 'com.neva.fork'

fork {
    config /* 'default', */ { // project forking configuration
        cloneFiles()
        moveFiles([
                "/com/company/app/example": "/{{projectGroup|substitute('.', '/')}}/{{projectName}}",
                "/example": "/{{projectName}}"
        ])
        replaceContents([
                "com.company.app.example": "{{projectGroup}}.{{projectName}}",
                'com.company.app': "{{projectGroup}}",
                "Example": "{{projectLabel}}",
                "example": "{{projectName}}",
        ])
    }
    config 'copy', {
        cloneFiles()
    }
    /*
    inPlaceConfig 'properties', { // predefined configuration for interactively generating 'gradle.properties' file
        copyTemplateFile("gradle/fork/gradle.properties.peb")
    }
    */
}
```

### Defining and executing configurations

Fork plugin allows to have multiple fork configurations defined. In above sample build script, there are 3 configurations defined:

1. Configuration *default* with the purpose of creating a new project based on existing one. In detail, it will:
    * Prompt to fill or update all variables detected in rules like `moveFiles`, `replaceContents` and occurrences of variables in text files.
    * Copy all project files respecting filtering defined in *.gitignore* files.
    * Rename directories using rules with properties injecting.
    * Replace contents using rules with properties injecting.
    
   Executable by command line (both equivalents, task *props* just runs *properties* configuration / is an alias):
   
   ```bash
   gradlew props
   gradlew fork -Pfork.config=properties
   ```
    
2. Predefined configuration named *properties* with the purpose of creating initial configuration before building project (generating *gradle.properties* file). In detail, it will:
    * Prompt to fill or update all variables detected in template file located at path *gradle/fork/gradle.properties.peb*.
    * Combine prompted variable values with template file to finally save a file containing user specific properties (like repository credentials etc). 
    
    Executable by command line (both equivalents, configuration named *default* can be skipped):
    
    ```bash
    gradlew fork
    gradlew fork -Pfork.config=default
    ```
    
3. Additional configuration named *copy* just for demonstrating purpose (which is only copying files / not updating them)

    ```bash
    gradlew fork -Pfork.config=copy
    ```

If there will be any more additional named configurations defined, it is possible to execute them all or only matching wildcard patterns (comma delimited):

```bash
gradlew fork -Pfork.config=*
gradlew fork -Pfork.config=no-search,no-auth
```

### Providing properties

Properties can be provided by (order makes precedence):
 
1. File which path could be specified as command line parameter:

    ```bash
    gradlew fork -Pfork.properties=fork.properties
    ```
    
    Such file should be in format:
    
    ```bash
    targetPath=../sample
    projectGroup=com.neva.app
    projectName=sample
    projectLabel=Sample
    ```
  
2. Each property defined separately as command line parameter:

    ```bash
    gradlew fork -PprojectName=sample -PprojectLabel=Sample -PtargetPath=../sample -Ppackage=com.neva.app.sample
    ```

3. GUI / properties dialog

    ![Props Dialog](docs/props-dialog.png)
    
    This dialog is always displayed to allow amending values provided by command line or properties file.
    
    To disable it, use command line parameter:
    
    ```bash
    gradlew fork -Pfork.interactive=false
    ```
  
4. Mixed approach.

### Defining project properties

Configuring of project properties can be enhanced by providing properties definitions which can be used for property value validation, e.g.:
```kotlin
fork {
    properties {
        define("enableSomething") { checkbox(defaultValue = true) }
        define("someUserName") { text(defaultValue = System.getProperty("user.name")) }
        define("projectGroup") { text(defaultValue = "org.neva") }
        define("someJvmOpts") {
            optional()
            text(defaultValue = "-server -Xmx1024m -XX:MaxPermSize=256M -Djava.awt.headless=true")
            validator { if (!property.value.startsWith("-")) error("This is not a JVM option!") }
        }
    }
}
```

#### Property definition
Property definition can consists of:
* type specification: `type = TYPE_NAME`
  * there are six types available: `TEXT` (default one), `CHECKBOX` (representing boolean), `PASSWORD` (encrypted by default), `SELECT` (list of options), `PATH` & `URL`.
  * there is default convention of type inference using property name (case insensitive):
    * ends with "password" -> `PASSWORD`
    * starts with "enable", "disable" -> `CHECKBOX`
    * ends with "enabled", "disabled" -> `CHECKBOX`
    * ends with "url" -> `URL`
    * ends with "path" -> `PATH`
    * else -> `TEXT`
* default value specification: `defaultValue = System.getProperty("user.name")`
  * if no value would be provided for property `defaultValue` is used
* declaring property as optional: `optional()`
  * by default all properties are required
* specifying custom validator: `validator = {if (!value.startsWith("-")) error("This is not a JVM option!")}`
  * by default `URL` & `PATH` properties gets basic validation which can be overridden or suppressed: `validator = {}`
  
#### Password encryption
By default passwords are kept plain text in `gradle.properties` file - which can be problematic when you have to input there your private passwords, etc. 

That's why Gradle Fork Plugin by default encrypts all `PASSWORD` properties (those which name ends with "password" or marked explicitly as password in their definition `password()`). This way generated `gradle.properties` file wont ever again contain any password plaintext.

To get access to this encrypted password in our build configuration simply apply `com.neva.fork.props` plugin:

```kotlin
import com.neva.gradle.fork.PropsExtension

allprojects {
    plugins.apply("com.neva.fork.props")
    
    val props = the<PropsExtension>()

    repositories {
        jcenter()
        maven {
            url = uri("https://nexus.company.com/content/groups/private")
            credentials {
                username = props.get("nexus.user")
                password = props.get("nexus.password")
            }
        }
    }
}
```   

### Sample output

After executing command `gradlew fork`, there will be a cloned project with correctly changed directory names, with replaced project name and label in text files (all stuff being previously performed manually).

<pre>
Cloning files from C:\Users\krystian.panek\Projects\example to ..\sample
Copying file from C:\Users\krystian.panek\Projects\example\.editorconfig to ..\sample\.editorconfig
...
Moving file from C:\Users\krystian.panek\Projects\example\apps\example\content.xml to ..\sample\apps\sample\content.xml
...
Replacing 'Example' with 'Sample' in file C:\Users\krystian.panek\Projects\sample\app\build.gradle
Replacing 'com.company.aem.example' with 'com.neva.aem.sample' in file C:\Users\krystian.panek\Projects\sample\app\common\build.gradle
Replacing 'example' with 'sample' in file C:\Users\krystian.panek\Projects\sample\app\common\src\main\content\META-INF\vault\filter.xml
</pre>

Then such forked project could be saved in VCS and each developer after cloning it could perform a setup very easily using command `gradlew props` to provide credentials to e.g Maven repositories, deployment servers etc before running application build that requires such data to be specified in *gradle.properties* file.

<pre>
Copying file from C:\Users\krystian.panek\Projects\sample\gradle\fork\gradle.properties to C:\Users\krystian.panek\Projects\sample\gradle.properties
Expanding properties in file C:\Users\krystian.panek\Projects\sample\gradle.properties
</pre>

## License

**Gradle Fork Plugin** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)

