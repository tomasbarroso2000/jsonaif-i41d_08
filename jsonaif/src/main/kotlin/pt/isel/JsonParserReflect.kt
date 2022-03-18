package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

object JsonParserReflect  : AbstractJsonParser() {

    /**
     * For each domain class we keep a Map<String, Setter> relating properties names with their setters.
     * This is for Part 2 of Jsonaif workout.
     */
    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()
    
    override fun parsePrimitive(tokens: JsonTokens, klass: KClass<*>): Any? {
        val string = tokens.popWordPrimitive()
        return basicParser[klass]?.let { it(string) }
    }

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any? {
        val constructor = klass.primaryConstructor
        val parameters = constructor?.parameters
        val parameterValues = mutableMapOf<KParameter, Any?>()
        tokens.pop(OBJECT_OPEN)
        while (tokens.current != OBJECT_END) {
            val propertyName = tokens.popWordFinishedWith(COLON).trim()
            val parameter = parameters?.find { it.name == propertyName }
            parameter?.let {
                parameterValues[parameter] =
                    parse(tokens, parameter.type.classifier as KClass<*>)
                if (tokens.current == COMMA) tokens.pop(COMMA)
            }
        }
        tokens.pop(OBJECT_END)
        return constructor?.callBy(parameterValues)
    }

}
