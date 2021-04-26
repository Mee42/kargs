package dev.mee42.kargs

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.collections.ArrayDeque
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf
import kotlin.reflect.typeOf

typealias ArgProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, T>>
typealias VarargProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, List<T>>>
typealias FlagProvider = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, Boolean>>
typealias SubcommandProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, T?>>



class Converter<T>(val convert: (String, KType) -> T)

open class Kargs {
    val arguments = mutableListOf<Argument>()
    val specifiedFlags = mutableSetOf<String>()
    val values = mutableMapOf<String, Any?>() // contains lookup name -> value
    val varargs = mutableMapOf<String, List<Any?>>()
    val floatingArgs = mutableListOf<Floating<*>>()
    var finalFloating: VariadicFloating<*>? = null
    val floatingValues = mutableMapOf<String, Any?>()
    val finalFloatingValues = mutableListOf<Any?>()
    val subcommands = mutableListOf<Subcommand>()
    var subcommandPicked: Pair<String, Subcommand>? = null // this will be set to something if a subcommand is seleceted, otherwise it'll stay null

    // BETTER BE THE FUCKING SAME
    val converterLookup = mutableMapOf<KType, Converter<*>>(
        // some reasonable defaults
        typeOf<String>()   to Converter { it, _ -> it },
        typeOf<Int>()      to Converter { it, _ -> it.toIntOrNull() ?: error("Tried to convert \"$it\" to integer, but could not parse") },
        typeOf<File>()     to Converter { it, _ -> File(it) },
        // TODO some way to signal a failed conversion
    )
    val converterIterativeLookup = mutableListOf<Pair<(KType) -> Boolean, Converter<*>>>(
        { it: KType -> (it.classifier as? KClass<*>)?.java?.isEnum ?: false } to Converter { it, type ->
            val enums = (type.classifier!! as KClass<Enum<*>>).java.enumConstants
            enums.firstOrNull { enum ->
                enum.name.lowercase() == it.lowercase()
            } ?: error("expected one of these: ${enums.toList()}, but got \"$it\"")
        }
    )


    companion object
}


inline fun <reified T> Kargs.getConverterForType(): Converter<T>? {
    val kType = typeOf<T>()
    val x = this.converterLookup[kType.withNullability(false)]
        ?: converterIterativeLookup.firstOrNull { it.first(kType) }?.second
    return x as Converter<T>?
}



open class Argument(
    val name: String,
    val longHelp: String?,
    val shortHelp: String?,
)

sealed class BasicNamed(
    name: String,
    val shortChar: Char?,
    shortHelp: String?,
    longHelp: String?,
): Argument(name, longHelp, shortHelp)

sealed class BasicNamedValue<T>(
    name: String,
    shortChar: Char?,
    shortHelp: String?,
    longHelp: String?,
    val converter: (String, KType) -> T,
    val type: KType): BasicNamed(name, shortChar, longHelp, shortHelp)

class NamedValue<T>(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    type: KType,
    converter: (String, KType) -> T,
    val default: Maybe<T>
): BasicNamedValue<T>(name, shortChar, longHelp, shortHelp, converter, type)

class Vararg<T>(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    type: KType,
    converter: ((String, KType) -> T),
    val requireRange: IntRange
): BasicNamedValue<T>(name, shortChar, longHelp, shortHelp, converter, type)



val KProperty<*>.sanitisedName: String
    get() = this.name.replace(Regex("""([A-Z])""")) { it.value.lowercase() + "-" }
/*


TODO:
 ✔ named values
 ✔ flags
 ✔ vararg values
 ✔ converting to types
 ✔ floating arguments (1, many)
 ✔ vararg values
 - allowing error state when converting to types
 - error handling in general
 - subcommands
 - help menu
 - passing name explicitly

 */
