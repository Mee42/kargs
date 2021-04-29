package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


private class FlagDelegate(private val name: String): ReadOnlyProperty<Kargs, Boolean> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): Boolean {
        return thisRef.specifiedFlags.contains(name)
    }
}


internal class Flag(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
): BasicNamed(name, shortChar, longHelp = longHelp, shortHelp = shortHelp)

fun Kargs.flag(
    shortChar: Char? = null,
    name: String? = null,
    longHelp: String? = null,
    shortHelp: String? = null,
): FlagProvider = PropertyDelegateProvider { thisRef, property ->
    val flag = Flag(
        shortChar = shortChar,
        longHelp = longHelp,
        shortHelp = shortHelp,
        name = name ?: property.sanitisedName
    )
    thisRef.arguments += flag
    return@PropertyDelegateProvider FlagDelegate(flag.name)
}
