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


#### Support for: *(unchecked are in progress)*
- [x] argument commands, long and short form
- [x] flags
- [x] variadic arguments
- [x] floating arguments
- [x] variadic floating arguments
- [x] default values
- [x] implicit conversions to non-String types
- [x] arbitrarily-nested subcommands
- [ ] automatic help menu handling
- [x] builtin help menu rendering, support for custom


##### Getting started
```kotlin
class Args: Kargs() {
    // your options, subcommands, flags, floating arguments, etc
}
fun main(args: Array<String>) {
    val args = Kargs.parse(args, ::Args)
}
```
Options can easily be specified by adding a variable bound to a function call. For example, to add an argument
```
val name by arg<String>()
// or
val name: String by arg()
```
This argument parser uses a space-based style for arguments, so this would be specified `--name Carson`. 
The name will be picked up automatically by the name of the variable, or you can specify it:
```
val theUsersName by arg<String>(name = "name")
```

Arguments and flags will take in a `shortChar` option as the first parameter:
```
val name by arg<String>('n')
```
Which would parse `-n Carson`.

Arguments specified without a default value are required, arguments with a default value are optional. To provide a default value:
```
val name by arg<String>(default = "")
```



*TODO: update help information.*


#### Add to your project:
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

