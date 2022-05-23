package pt.isel

import kotlin.reflect.KClass

interface JsonParser {

    fun <T : Any> parse(source: String, klass: KClass<T>): T?

    fun <T : Any> parseArray(source: String, klass: KClass<T>): List<T?>

    fun <T : Any> parseSequence(json: String, klass: KClass<T>): Sequence<T?>

}

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
