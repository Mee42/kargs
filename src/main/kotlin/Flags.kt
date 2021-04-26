package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FlagDelegate: ReadOnlyProperty<Kargs, Boolean> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): Boolean {
        return thisRef.specifiedFlags.contains(property.sanitisedName)
    }
}


class Flag(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
): BasicNamed(name, shortChar, longHelp, shortHelp)

fun Kargs.flag(
    shortChar: Char? = null,
    longHelp: String? = null,
    shortHelp: String? = null,
): FlagProvider = PropertyDelegateProvider { thisRef, property ->
    val flag = Flag(
        shortChar = shortChar,
        longHelp = longHelp,
        shortHelp = shortHelp,
        name = property.sanitisedName
    )
    thisRef.arguments += flag
    return@PropertyDelegateProvider FlagDelegate()
}
