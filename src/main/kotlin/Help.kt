package dev.mee42.kargs

fun genHelpMenu(
    sayUsage: Boolean,
    programName: String,
    command: Kargs,
    padTo: Int = 40,
    showExtensiveSubCommands: Boolean
): String {
    val lineOne = (if(sayUsage) "Usage: " else "") + programName +
            (if(command.arguments.size > 0) " [OPTIONS]" else "") +
            (if(command.floatingArgs.size > 0)
                command.floatingArgs.joinToString(" ", prefix = " ") { "[${it.name}]" }
            else "") +
            (command.finalFloating?.name?.uppercase()?.let { " [$it]..." } ?: "") +
            (if(command.subcommands.size > 0) " [SUBCOMMAND]" else "")
    var str = lineOne + "\n"
    if(command.arguments.size > 0) {
        str += "Options:\n"
        val arguments = command.arguments + listOf(command.finalFloating).filterNotNull()
        for(argument in arguments.sortedBy { it.name }) {
            var line = when (argument) {
                is BasicNamed ->
                    "\t" + (if (argument.shortChar == null) "" else "-${argument.shortChar}, ") + "--${argument.name}"
                is Floating<*> ->
                    "\t[" + argument.name.uppercase() + (if(argument.shortHelp != null) ": " + argument.shortHelp else "") + "]"
                is VariadicFloating<*> ->
                    "\t[" + argument.name.uppercase() + "" + (if(argument.shortHelp != null) ": " + argument.shortHelp else "") + "]..."
                else -> error("idk how to print $argument")
            }
            line += if(argument is BasicNamedValue<*>) " <" + (argument.shortHelp ?: "arg") + ">" else ""
            line = line.padEnd(padTo, ' ')
            line += argument.longHelp ?: ""
            str += line + "\n"
        }
    }
    if(command.subcommands.size > 0 && showExtensiveSubCommands) {
        str += "\nSubcommands:\n\n"
        for(subcommand in command.subcommands.sortedBy { it.name }) {
            str += genHelpMenu(false, "$programName ${subcommand.name}", subcommand, padTo, false) + "\n"
        }
    } else if(command.subcommands.size > 0) {
        str += "Subcommands: " + command.subcommands.joinToString(", ") { it.name.lowercase() }
    }
    return str
}