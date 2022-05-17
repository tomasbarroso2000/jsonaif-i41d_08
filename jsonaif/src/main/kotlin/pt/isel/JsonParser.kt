package pt.isel

import kotlin.reflect.KClass

interface JsonParser {

    fun <T> parse(source: String): T

    fun <T> parseArray(source: String): List<T>

}
