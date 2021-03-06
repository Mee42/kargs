package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal typealias FloatingProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, T>>
internal typealias VariadicFloatingProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, List<T>>>




internal class Floating<T>(
    name: String,
    longHelp: String?,
    shortHelp: String?,
    val type: KType,
    val converter: (String, KType) -> T,
    val default: Maybe<T>,
): Argument(name, longHelp, shortHelp)

internal class VariadicFloating<T>(
    name: String,
    longHelp: String?,
    shortHelp: String?,
    val type: KType,
    val converter: (String, KType) -> T,
): Argument(name, longHelp, shortHelp)
// if default is Maybe.Nothing, then the argument is required
// otherwise it is not


fun <T> Internal.floatWrapper(
    shortHelp: String?,
    longHelp: String?,
    default: Maybe<T>,
    name: String?,
    converter: ((String) -> T)? = null,
    kType: KType,
): FloatingProvider<T> = PropertyDelegateProvider { thisRef, property ->

    if(thisRef.finalFloating != null) {
        error("can not define floating arguments after a floatMany() has been defined")
    }
    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: getConverterForType<T>(kType)?.convert
        ?: error("can't convert string to ${kType.classifier}")

    val floating = Floating(
        shortHelp = shortHelp,
        longHelp = longHelp,
        converter = newConverter,
        type = kType,
        name = name ?: property.sanitisedName,
        default = default
    )
    if(default is Maybe.Nothing) {
        // everything existing needs to be required if this one is to be required
        if(thisRef.floatingArgs.any { it.default is Maybe.Just }) {
            error("can't have a required floating argument after a non-required floating argument")
        }
    }
    thisRef.floatingArgs += floating
    thisRef.arguments += floating
    return@PropertyDelegateProvider FloatingDelegate<T>(floating.name)
}

// has NO default and is REQUIRED
inline fun <reified T> Kargs.float(
    shortHelp: String? = null,
    longHelp: String? = null,
    name: String? = null,
    noinline converter: ((String) -> T)? = null,
): FloatingProvider<T> = Internal.floatWrapper(
    shortHelp = shortHelp, longHelp = longHelp, converter = converter,
    default = Maybe.Nothing(), name = name, kType = typeOf<T>()
)
// has a default and is NOT REQUIRED
inline fun <reified T> Kargs.float(
    shortHelp: String? = null,
    longHelp: String? = null,
    default: T,
    name: String? = null,
    noinline converter: ((String) -> T)? = null,
): FloatingProvider<T> = Internal.floatWrapper(
    shortHelp, longHelp,
    default = Maybe.Just(default), name, converter,
    kType = typeOf<T>()
)


inline fun <reified T> Kargs.floatMany(
    shortHelp: String? = null,
    longHelp: String? = null,
    name: String? = null,
    noinline converter: ((String) -> T)? = null,
):VariadicFloatingProvider<T> = Internal.floatManyWrapper(shortHelp, longHelp, name, converter, typeOf<T>())


fun <T> Internal.floatManyWrapper(
    shortHelp: String? = null,
    longHelp: String? = null,
    name: String? = null,
    converter: ((String) -> T)? = null,
    kType: KType,
):VariadicFloatingProvider<T> = PropertyDelegateProvider { thisRef, property ->

    if(thisRef.finalFloating != null) {
        error("can not define more than one variadic floating argument")
    }
    val newConverter = converter?.let { f -> { it, _ -> f(it) } } // add an ignored type argument that we don't expose to the user
        ?: getConverterForType<T>(kType)?.convert
        ?: error("can't convert string to ${kType.classifier}")

    val arg = VariadicFloating(
        shortHelp = shortHelp,
        longHelp = longHelp,
        converter = newConverter,
        type = kType,
        name = name ?: property.sanitisedName
    )
    thisRef.finalFloating = arg
    return@PropertyDelegateProvider VariadicFloatingDelegate<T>()
}


private class FloatingDelegate<T>(private val name: String): ReadOnlyProperty<Kargs, T> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): T {
        return thisRef.floatingValues[name] as T
    }
}
private class VariadicFloatingDelegate<T>: ReadOnlyProperty<Kargs, List<T>> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): List<T> {
        return thisRef.finalFloatingValues as List<T>
    }
}