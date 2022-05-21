package pt.isel

import kotlin.reflect.KClass

interface JsonParser {

    fun <T : Any> parse(source: String, klass: KClass<T>): T

    fun <T : Any> parseArray(source: String, klass: KClass<T>): List<T>

}
