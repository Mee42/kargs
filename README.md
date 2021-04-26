# Kargs: An Kotlin argument parser

[![Release](https://jitpack.io/v/mee42/kargs.svg)](https://jitpack.io/#mee42/kargs)

```kotlin
import dev.mee42.kargs.*

class Args: Kargs() {
    val verbose by flag('v', longHelp = "Enable verbose output")
    val help by flag('h', longHelp = "Display this information")
    val initialDirectory by arg('c',
        shortHelp="path",
        longHelp = "Run with <path> as the working directory",
        default = File(System.getProperty("user.dir")),
        converter = ::File
    )
    val status by subcommand<StatusCommand>()
    val add by subcommand<AddCommand>()
}

class StatusCommand: Subcommand("status") {
    val short by flag('s', longHelp = "Give the output in the short format")
    val branch by flag('b', longHelp = "Show the branch even when in short format")
    val columns by arg<Int?>(default = 80, longHelp = "The number of columns to constraint output in", shortHelp = "cols")
}
class AddCommand: Subcommand("add") {
    val dryRun by flag('n', longHelp = "Don't actually add any file(s), just show if they exist and/or will be ignored")
    val files by floatMany<File>(longHelp = "The files to add")
}

```
A fluent API for parsing command line arguments in kotlin.
Support for:
- [x] argument commands
- [x] flags
- [x] variadic arguments
- [x] floating arguments
- [x] variadic floating arguments
- [x] default values
- [x] implicit conversions to non-String types
- [x] arbitrarily-nested subcommands
- [x] help menu rendering

Guide:









Add to your project:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.mee42:kargs:master-SNAPSHOT'
}
```
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation 'com.github.mee42:kargs:master-SNAPSHOT'
}
```
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependency>
    <groupId>com.github.mee42</groupId>
    <artifactId>kargs</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

