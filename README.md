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


#### Getting started
```kotlin
class Args: Kargs() {
    // your options, subcommands, flags, floating arguments, etc
}
fun main(args: Array<String>) {
    val args = Kargs.parse(args, ::Args)
}
```
#### Arguments
Options can easily be specified by adding a variable bound to a function call. For example, to add an argument
```kotlin
val name by arg<String>() // --name Carson
// or
val name: String by arg()
// The '--name' name will be picked up automatically by the name of the variable, or you can specify it
val theUsersName by arg<String>(name = "foobar") --foobar Carson

//  Arguments and flags will take in a `shortChar` option as the first parameter:
val name by arg<String>('n') // -n Carson

// Arguments specified without a default value are required, arguments with a default value are optional.
val name by arg<String>(default = "") // optional

// you can use specify a different type than String for all options with values.
val file: File by arg<File>('f') // --file test.txt

// converters to some types are predefined, or can be specified inline
val file by arg<File>('f', converter = ::File)
// see below for more converter information

// TODO more information on help messages

// arguments that can be specified more than once can use the vararg function
val files: List<File> by vararg<File>('f', converter = ::File, name = "file")
```
Flags are like arguments, but take no arguments. The type is always boolean, and they can't be varadic
```
val help by flag('h')
```
Floating arguments hold


*TODO: make code changes and add help information.*


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
    implementation("com.github.mee42:kargs:master-SNAPSHOT")
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

