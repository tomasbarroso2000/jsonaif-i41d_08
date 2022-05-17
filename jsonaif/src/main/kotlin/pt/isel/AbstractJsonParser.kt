package pt.isel

abstract class AbstractJsonParser : JsonParser {

    override fun <T> parse(source: String): T {
        return parse(JsonTokens(source))
    }

    fun <T> parse(tokens: JsonTokens): T = when (tokens.current) {
        OBJECT_OPEN -> parseObject(tokens)
        DOUBLE_QUOTES -> parseString(tokens) as T
        else -> parsePrimitive(tokens)
    }

    abstract fun <T> parsePrimitive(tokens: JsonTokens): T

    abstract fun <T> parseObject(tokens: JsonTokens): T

    private fun parseString(tokens: JsonTokens): String {
        tokens.pop(DOUBLE_QUOTES) // Discard double quotes "
        return tokens.popWordFinishedWith(DOUBLE_QUOTES)
    }

    override fun <T> parseArray(source: String): List<T> {
        return parseArray(JsonTokens(source))
    }

    private fun <T> parseArray(tokens: JsonTokens): List<T> {
        val list = mutableListOf<T>()
        tokens.pop(ARRAY_OPEN) // Discard square brackets [ ARRAY_OPEN
        while (tokens.current != ARRAY_END) {
            val v: T = parse(tokens)
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
