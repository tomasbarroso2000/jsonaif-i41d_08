package pt.isel

import java.io.File
import kotlin.reflect.KClass

abstract class AbstractJsonParser : JsonParser {

    override fun <T : Any> parse(source: String, klass: KClass<T>): T? {
        return parse(JsonTokens(source), klass)
    }

    override fun <T : Any> parseArray(source: String, klass: KClass<T>): List<T?> {
        return parseArray(JsonTokens(source), klass)
    }

    override fun <T : Any> parseSequence(source: String, klass: KClass<T>): Sequence<T?> {
        return parseSequence(JsonTokens(source), klass)
    }

    override fun <T : Any> parseFolderEager(path: String, klass: KClass<T>): List<T?> {
        val listOfFiles = mutableListOf<T?>()
        File(path).listFiles()?.forEach {
            val text = it.readText()

            // Throw exception if the object is not compatible with T
            val parsedObject = parseObject(JsonTokens(text), klass) ?: throw Exception("Object is not compatible")
            listOfFiles.add(parsedObject)
        }
        return listOfFiles
    }

    override fun <T : Any> parseFolderLazy(path: String, klass: KClass<T>): Sequence<T?> = sequence {
        File(path).listFiles()?.forEach {
            val text = it.readText()

            // Throw exception if the object is not compatible with T
            val parsedObject = parseObject(JsonTokens(text), klass) ?: throw Exception("Object is not compatible")
            yield(parsedObject)
        }
    }

    fun <T : Any> parse(tokens: JsonTokens, klass: KClass<T>): T? {
        return when (tokens.current) {
            OBJECT_OPEN -> parseObject(tokens, klass)
            DOUBLE_QUOTES -> parseString(tokens)
            else -> parsePrimitive(tokens, klass)
        }
    }

    abstract fun <T : Any> parsePrimitive(tokens: JsonTokens, klass: KClass<T>): T

    abstract fun <T : Any> parseObject(tokens: JsonTokens, klass: KClass<T>): T?

    private fun <T : Any> parseSequence(tokens: JsonTokens, klass: KClass<T>): Sequence<T?> = sequence {
        tokens.pop(ARRAY_OPEN) // Discard square brackets [ ARRAY_OPEN
        while (tokens.current != ARRAY_END) {
            val v: T? = parse(tokens, klass)
            yield(v)
            if (tokens.current == COMMA) // The last element finishes with ] rather than a comma
                tokens.pop(COMMA) // Discard COMMA
            else break
            tokens.trim()
        }
        tokens.pop(ARRAY_END) // Discard square bracket ] ARRAY_END
    }

    private fun <T> parseString(tokens: JsonTokens): T {
        tokens.pop(DOUBLE_QUOTES) // Discard double quotes "
        return tokens.popWordFinishedWith(DOUBLE_QUOTES) as T
    }

    fun <T : Any> parseArray(tokens: JsonTokens, klass: KClass<T>): List<T?> {
        val list = mutableListOf<T?>()
        tokens.pop(ARRAY_OPEN) // Discard square brackets [ ARRAY_OPEN
        while (tokens.current != ARRAY_END) {
            val v: T? = parse(tokens, klass)
            list.add(v)
            if (tokens.current == COMMA) // The last element finishes with ] rather than a comma
                tokens.pop(COMMA) // Discard COMMA
            else break
            tokens.trim()
        }
        tokens.pop(ARRAY_END) // Discard square bracket ] ARRAY_END
        return list
    }
}
