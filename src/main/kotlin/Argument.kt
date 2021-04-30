package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf


fun <T> Internal.argMain(
    name: String?,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    default: Maybe<T>,
    converter: ((String) -> T)?,
    kType: KType,
):ArgProvider<T>  = PropertyDelegateProvider { thisRef, property ->
    // the converter ONLY runs when a value is specified
    //
    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: getConverterForType<T>(kType)?.convert
        ?: error("can't convert string to ${kType.classifier}")


    val arg = NamedValue(
        name = name ?: property.sanitisedName,
        shortChar = shortChar,
        longHelp = longHelp,
        shortHelp = shortHelp,
        converter = newConverter,
        type = kType,
        default = default
    )
    thisRef.arguments += arg
    return@PropertyDelegateProvider ArgumentDelegate<T>(arg.name)
}

// can be nullable, does NOT have a default parameter
inline fun <reified T: Any?> Kargs.arg(
    shortChar: Char? = null,
    name: String? = null,
    longHelp: String? = null,
    shortHelp: String? = null,
    noinline converter: ((String) -> T)? = null
): ArgProvider<T> = Internal.argMain(name, shortChar, longHelp, shortHelp, default = Maybe.Nothing(), converter, typeOf<T>())

// can be nullable, DOES have a a default parameter
inline fun <reified T: Any?> Kargs.arg(
    shortChar: Char? = null,
    name: String? = null,
    longHelp: String? = null,
    shortHelp: String? = null,
    default: T,
    noinline converter: ((String) -> T)? = null
): ArgProvider<T> = Internal.argMain(name, shortChar, longHelp = longHelp, shortHelp = shortHelp, default = Maybe.Just(default), converter, typeOf<T>())



inline fun <reified T> Kargs.vararg(
    shortChar: Char? = null,
    name: String? = null,
    shortHelp: String? = null,
    longHelp: String? = null,
    requireRange: IntRange = 0..Int.MAX_VALUE,
    noinline converter: ((String) -> T)? = null,
): VarargProvider<T> = Internal.varargWrapper(typeOf<T>(), shortChar, name, shortHelp, longHelp, converter, requireRange)

fun <T> Internal.varargWrapper(
    kType: KType,
    shortChar: Char? = null,
    name: String? = null,
    shortHelp: String? = null,
    longHelp: String? = null,
    requireRange: IntRange = 0..Int.MAX_VALUE,
    converter: ((String) -> T)? = null,
): VarargProvider<T> = PropertyDelegateProvider { thisRef, property ->

    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: getConverterForType<T>(kType)?.convert
        ?: error("can't convert string to ${kType.classifier}")

    val vararg = Vararg(
        shortChar = shortChar,
        shortHelp = shortHelp,
        longHelp = longHelp,
        type = kType,
        converter = newConverter,
        name = name ?: property.sanitisedName,
        requireRange = requireRange
    )
    thisRef.arguments.add(vararg)
    return@PropertyDelegateProvider VarargDelegate<T>(vararg.name)
}


private class ArgumentDelegate<T>(private val name: String): ReadOnlyProperty<Kargs, T> {
    override operator fun getValue(thisRef: Kargs, property: KProperty<*>): T {
        return thisRef.values[name] as T
    }
}
private class VarargDelegate<T>(private val name: String): ReadOnlyProperty<Kargs, List<T>> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): List<T> {
        return thisRef.varargs[name]?.let { it as List<T> } ?: emptyList()
    }
}
