package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf


open class Subcommand(val name: String): Kargs()

inline fun <reified T: Subcommand> subcommand(
    noinline constructor: () -> T = { T::class.primaryConstructor!!.call() }
): SubcommandProvider<T> = Internal.subcommand(typeOf<T>(), constructor)

fun <T: Subcommand> Internal.subcommand(
    kType: KType,
    constructor: () -> T = { (kType.classifier as KClass<T>).primaryConstructor!!.call() },
): SubcommandProvider<T> = PropertyDelegateProvider { thisRef, _ ->
    val subcommand = constructor()
    thisRef.subcommands += subcommand
    SubcommandDelegate(subcommand.name)
}

private class SubcommandDelegate<T: Subcommand>(val name: String): ReadOnlyProperty<Kargs, T?> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): T? {
        if(thisRef.subcommandPicked == null) return null
        if(thisRef.subcommandPicked!!.first != name) return null // if another subcommand was picked
        return thisRef.subcommandPicked!!.second as T?
    }
}
