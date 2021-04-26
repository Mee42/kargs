package test


import dev.mee42.kargs.*
import java.io.File

class Args: Kargs() {
    val verbose by flag('v', longHelp = "Enable verbose output")
    val help by flag('h', longHelp = "Display this information")
    val version by flag(longHelp = "Display version information")
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

fun main() {
    val args = "--help".split(" ").toTypedArray()
    val parsed = Kargs.parse(args, ::Args)
    println(genHelpMenu(true, "git", parsed, 40, true))
    parsed.status?.let(::statusCommand)
}
fun statusCommand(parsed: StatusCommand) {
    val branchName = "master"
    val modified = "someModifiedFile.txt"
    val untracked = "untracked.txt"

    if(parsed.short) {
        if(parsed.branch) {
            println("## $branchName")
        }
        println("M $modified")
        println("? $untracked")
    } else {
        println("""
On branch $branchName

Changes not staged for commit:
       (use "git add <file>..." to update what will be committed)
       (use "git restore <file>..." to discard changes in working directory)
            modified:  $modified 

Untracked files:
       (use "git add <file>..." to include in what will be committed)
            $untracked

no changes added to commit (use "git add" and/or "git commit -a")     
        """.trimIndent())

    }
}








