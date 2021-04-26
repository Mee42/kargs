package test


import dev.mee42.kargs.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.primaryConstructor

class Args: Kargs() {
    val verbose by flag('v')
    val help by flag('h')
    val version by flag('V')
    val status by subcommand<StatusCommand>()
}
class StatusCommand: Subcommand("status") {
    val short by flag('s')
    val branch by flag('b')
    val columns by arg<Int?>(default = 80)
}

fun main() {
    val args = "-v status".split(" ").toTypedArray()
    val parsed = Kargs.parse(args, ::Args)
    parsed.status?.let(::statusCommand)
}
fun statusCommand(parsed: StatusCommand) {
    if(parsed.short) {
        printn("??")
    }
}








