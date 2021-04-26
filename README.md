# Kargs: An Kotlin argument parser

[![Release](https://jitpack.io/v/mee42/kargs.svg)](https://jitpack.io/#mee42/kargs)

```kotlin
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


