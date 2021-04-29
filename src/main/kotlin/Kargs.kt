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

internal typealias ArgProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, T>>
internal typealias VarargProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, List<T>>>
internal typealias FlagProvider = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, Boolean>>
internal typealias SubcommandProvider<T> = PropertyDelegateProvider<Kargs, ReadOnlyProperty<Kargs, T?>>

object Internal

class Converter<T>(val convert: (String, KType) -> T)

open class Kargs {
    internal val arguments = mutableListOf<Argument>()
    internal val specifiedFlags = mutableSetOf<String>()
    internal val values = mutableMapOf<String, Any?>() // contains lookup name -> value
    internal val varargs = mutableMapOf<String, List<Any?>>()
    internal val floatingArgs = mutableListOf<Floating<*>>()
    internal var finalFloating: VariadicFloating<*>? = null
    internal val floatingValues = mutableMapOf<String, Any?>()
    internal val finalFloatingValues = mutableListOf<Any?>()
    internal val subcommands = mutableListOf<Subcommand>()
    internal var subcommandPicked: Pair<String, Subcommand>? = null // this will be set to something if a subcommand is seleceted, otherwise it'll stay null
    companion object {
        // BETTER BE THE SAME
        internal val converterLookup = mutableMapOf<KType, Converter<*>>(
            // some reasonable defaults
            typeOf<String>() to Converter { it, _ -> it },
            typeOf<Int>() to Converter { it, _ ->
                it.toIntOrNull() ?: error("Tried to convert \"$it\" to integer, but could not parse")
            },
            typeOf<File>() to Converter { it, _ -> File(it) },
            // TODO some way to signal a failed conversion
        )

        internal val converterIterativeLookup = mutableListOf<Pair<(KType) -> Boolean, Converter<*>>>(
            { it: KType -> (it.classifier as? KClass<*>)?.java?.isEnum ?: false } to Converter { it, type ->
                val enums = (type.classifier!! as KClass<Enum<*>>).java.enumConstants
                enums.firstOrNull { enum ->
                    enum.name.lowercase() == it.lowercase()
                } ?: error("expected one of these: ${enums.toList()}, but got \"$it\"")
            }
        )
    }
}
internal fun <T> getConverterForType(kType: KType): Converter<T>? {
    val x = Kargs.converterLookup[kType.withNullability(false)]
        ?: Kargs.converterIterativeLookup.firstOrNull { it.first(kType) }?.second
    return x as Converter<T>?
}


internal open class Argument(
    val name: String,
    val longHelp: String?,
    val shortHelp: String?,
)

internal sealed class BasicNamed(
    name: String,
    val shortChar: Char?,
    shortHelp: String?,
    longHelp: String?,
): Argument(name, longHelp, shortHelp)

internal sealed class BasicNamedValue<T>(
    name: String,
    shortChar: Char?,
    shortHelp: String?,
    longHelp: String?,
    val converter: (String, KType) -> T,
    val type: KType): BasicNamed(name, shortChar, longHelp, shortHelp)

internal class NamedValue<T>(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    type: KType,
    converter: (String, KType) -> T,
    val default: Maybe<T>
): BasicNamedValue<T>(name, shortChar, longHelp, shortHelp, converter, type)

internal class Vararg<T>(
    name: String,
    shortChar: Char?,
    longHelp: String?,
    shortHelp: String?,
    type: KType,
    converter: ((String, KType) -> T),
    val requireRange: IntRange
): BasicNamedValue<T>(name, shortChar, longHelp, shortHelp, converter, type)



internal val KProperty<*>.sanitisedName: String
    get() = this.name.replace(Regex("""([A-Z])""")) { "-" + it.value.lowercase() }




/*
TODO:
 ✔ named values
 ✔ flags
 ✔ vararg values
 ✔ converting to types
 ✔ floating arguments (1, many)
 ✔ vararg values
 - allow graceful errors when converting
 - error handling in general
 ✔ subcommands
 ✔ help menu
 ✔ passing name explicitly, remove all access to property name besides the initial access
 - make things that need to be private, private
 - add options to add a global converter
 - split help into just "description" and "longHelp", and add "argHelp" (but not for flags), as well as a help for subcommands
    description is for the -A --AAA      <description goes here> format
    longHelp is for the man-page-like format?
 - figure out exactly how the help api will work ^^^
 -
 - make look nice
 - publish??

 */
