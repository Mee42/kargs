package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf


inline fun <reified T> Kargs.argMain(
    name: String?,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    default: Maybe<T>,
    noinline converter: ((String) -> T)?
):ArgProvider<T>  = PropertyDelegateProvider { thisRef, property ->
    // the converter ONLY runs when a value is specified
    //
    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: thisRef.getConverterForType<T>()?.convert
        ?: error("can't convert string to ${typeOf<T>().classifier}")


    val arg = NamedValue(
        name = name ?: property.sanitisedName,
        shortChar = shortChar,
        longHelp = longHelp,
        shortHelp = shortHelp,
        converter = newConverter,
        type = typeOf<T>(),
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
): ArgProvider<T> = argMain(name, shortChar, longHelp, shortHelp, default = Maybe.Nothing(), converter)

// can be nullable, DOES have a a default parameter
inline fun <reified T: Any?> Kargs.arg(
    shortChar: Char? = null,
    name: String? = null,
    longHelp: String? = null,
    shortHelp: String? = null,
    default: T,
    noinline converter: ((String) -> T)? = null
): ArgProvider<T> = argMain(name, shortChar, longHelp = longHelp, shortHelp = shortHelp, default = Maybe.Just(default), converter)

inline fun <reified T> Kargs.vararg(
    shortChar: Char? = null,
    name: String? = null,
    shortHelp: String? = null,
    longHelp: String? = null,
    noinline converter: ((String) -> T)? = null,
    requireRange: IntRange = 0..Int.MAX_VALUE,
): VarargProvider<T> = PropertyDelegateProvider { thisRef, property ->

    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: thisRef.getConverterForType<T>()?.convert
        ?: error("can't convert string to ${typeOf<T>().classifier}")

    val vararg = Vararg(
        shortChar = shortChar,
        shortHelp = shortHelp,
        longHelp = longHelp,
        type = typeOf<T>(),
        converter = newConverter,
        name = name ?: property.sanitisedName,
        requireRange = requireRange
    )
    thisRef.arguments.add(vararg)
    return@PropertyDelegateProvider VarargDelegate<T>(vararg.name)
}


class ArgumentDelegate<T>(private val name: String): ReadOnlyProperty<Kargs, T> {
    override operator fun getValue(thisRef: Kargs, property: KProperty<*>): T {
        return thisRef.values[name] as T
    }
}
class VarargDelegate<T>(private val name: String): ReadOnlyProperty<Kargs, List<T>> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): List<T> {
        return thisRef.varargs[name]?.let { it as List<T> } ?: emptyList()
    }
}
