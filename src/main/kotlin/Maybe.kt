package dev.mee42.kargs

sealed class Maybe<out T> {
    class Just<out T>(val value: T): Maybe<T>()
    class Nothing<out T>: Maybe<T>()
}