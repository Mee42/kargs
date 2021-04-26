package dev.mee42.kargs

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor


open class Subcommand(val name: String): Kargs()



inline fun <reified T: Subcommand> subcommand(
    noinline constructor: () -> T = { T::class.primaryConstructor!!.call() }
): SubcommandProvider<T> = PropertyDelegateProvider { thisRef, property ->
    val subcommand = constructor()
    thisRef.subcommands += subcommand
    SubcommandDelegate(subcommand.name)
}

class SubcommandDelegate<T: Subcommand>(val name: String): ReadOnlyProperty<Kargs, T?> {
    override fun getValue(thisRef: Kargs, property: KProperty<*>): T? {
        if(thisRef.subcommandPicked == null) return null
        if(thisRef.subcommandPicked!!.first != name) return null // if another subcommand was picked
        return thisRef.subcommandPicked!!.second as T?
    }
}
