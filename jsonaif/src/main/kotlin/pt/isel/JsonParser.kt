package pt.isel

import kotlin.reflect.KClass

interface JsonParser {

    fun <T : Any> parse(source: String, klass: KClass<T>): T?

    fun <T : Any> parseArray(source: String, klass: KClass<T>): List<T?>

    fun <T : Any> parseSequence(source: String, klass: KClass<T>): Sequence<T?>

    fun <T : Any> parseFolderEager(path: String, klass: KClass<T>): List<T?>

    fun <T : Any> parseFolderLazy(path: String, klass: KClass<T>): Sequence<T?>

}

// Extension functions to avoid the need of casting after calling the functions

inline fun <reified T : Any> JsonParser.parse(source: String): T? {
    val klass = T::class
    return this.parse(source, klass)
}

inline fun <reified T : Any> JsonParser.parseArray(source: String): List<T?> {
    val klass = T::class
    return this.parseArray(source, klass)
}

inline fun <reified T : Any> JsonParser.parseSequence(source: String): Sequence<T?> {
    val klass = T::class
    return this.parseSequence(source, klass)
}

inline fun <reified T : Any> JsonParser.parseFolderEager(path: String): List<T?> {
    val klass = T::class
    return this.parseFolderEager(path, klass)
}

inline fun <reified T : Any> JsonParser.parseFolderLazy(path: String): Sequence<T?> {
    val klass = T::class
    return this.parseFolderLazy(path, klass)
}