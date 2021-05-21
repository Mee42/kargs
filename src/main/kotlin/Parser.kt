package dev.mee42.kargs

import java.io.File


private fun parseOutArgument(t:Kargs, argument: BasicNamedValue<*>, value: String, preceding: String) {
    if (value.startsWith("-") && value != "-") error("was expecting a value after \"$preceding\", but got \"$value\"")
    // TODO check for error in conversion
    val convertedValue = argument.converter(value, argument.type)
    when (argument) {
        is NamedValue<*> -> t.values[argument.name] = convertedValue
        is Vararg<*> -> t.varargs[argument.name] = (t.varargs[argument.name] ?: emptyList()) + convertedValue
    }

}

fun envFile(name: String): List<String> = File(name).readLines()

fun <T: Kargs> Kargs.Companion.parse(args: Array<String>, envVarName: String? = null, constructor: () -> T): T {
    val configFile = envVarName?.let { System.getProperty(it, null) }
        ?.takeUnless(String::isBlank)
        ?.let(::envFile) ?: emptyList()
    val tokens = ArrayDeque(configFile + args.toList())
    val t = constructor()


    while(tokens.isNotEmpty()) {
        val first = tokens.removeFirst()
        if(first.startsWith("--") && first != "--") {
            val flagName = first.substring(2)
            val argument = t.arguments.firstOrNull { it.name == flagName } ?: error("\"--$flagName\" not recognized")
            when (argument) {
                is BasicNamedValue<*> -> {
                    val value = tokens.removeFirst()
                    parseOutArgument(t, argument, value, first)
                }
                is Flag -> t.specifiedFlags.add(argument.name)
                else -> error("unknown argument type $argument")
            }
        } else if(first.startsWith("-") && first != "-" && first != "--") {
            // single char thing
            var string = first
            while(true) { // loop
                val char = string[1]
                val argument = t.arguments.firstOrNull { it is BasicNamed && it.shortChar == char } ?: error("\"-$char\" not recognized")
                if (argument is BasicNamedValue<*>) {
                    val restOfFirst = string.substring(2) // "-O3" -> "3"
                    val valueString =
                        restOfFirst.ifBlank { tokens.removeFirst() } // if it's blank, get the next one
                    parseOutArgument(t, argument, valueString, string)
                    break
                } else if (argument is Flag) {
                    t.specifiedFlags.add(argument.name)
                    if(string.length == 2) break
                    string = "-" + string.substring(2)
                    continue
                }
                error("unknown argument type $argument")
            }
        } else {
            if(t.subcommands.any { it.name == first }) {
                // subcommands
                val subcommand = t.subcommands.first { it.name == first }
                t.subcommandPicked = first to subcommand
                // then we parse into it by passing it the rest of the arguments
                Kargs.parse(tokens.toTypedArray()) { subcommand }
                // pls don't consume these tokens any more
                return t
            } else {
                // floating commands
                val existingFloatingArgumentCount =
                    t.floatingValues.size // how many floating valus have been recorded so far
                if (existingFloatingArgumentCount == t.floatingArgs.size) {
                    if (t.finalFloating == null) error("ran into an unxpected floating argument: $first")
                    val arg = t.finalFloating!!
                    val value = arg.converter(first, arg.type)
                    t.finalFloatingValues += value
                } else {
                    val matchingArgument = t.floatingArgs[existingFloatingArgumentCount]
                    val value = matchingArgument.converter(first, matchingArgument.type)
                    t.floatingValues[matchingArgument.name] = value
                }
            }
        }
    }
    for(argument in t.arguments) {
        // check all the other arguments, compute default values or error if not specified
        if(t.values.containsKey(argument.name)) continue

        if(argument is NamedValue<*>) {
            if (argument.default is Maybe.Just) {
                t.values[argument.name] = argument.default.value
            } else {
                error("missing --${argument.name}")
            }
        } else if(argument is Flag) {
            // do nothing
        } else if(argument is Vararg<*>) {
            val list = t.varargs[argument.name] ?: emptyList()
            if(list.size !in argument.requireRange) {
                if(argument.requireRange.last == Int.MAX_VALUE) {
                    error("Need at least ${argument.requireRange.first} parameters for ${argument.name}")
                } else {
                    error("was expecting ${argument.requireRange} --${argument.name} options, but got ${list.size}")
                }
            }
        } else if(argument is Floating<*>) {
            if (!t.floatingValues.containsKey(argument.name)) {
                if (argument.default is Maybe.Just) {
                    // we have a default value we can use
                    t.floatingValues[argument.name] = argument.default.value
                } else {
                    error("Expecting a value for the floating argument [${argument.name}], but didn't find it")
                }
            }
        } else {
            error("unknown argument type: $argument")
        }
    }
    for(floating in t.floatingArgs) {
        if(!t.floatingValues.containsKey(floating.name)){
            if(floating.default is Maybe.Just) {
                // we have a default value we can use
                t.floatingValues[floating.name] = floating.default.value
            } else {
                error("Expecting a value for the floating argument [${floating.name}], but didn't find it")
            }
        }
    }
    return t
}
