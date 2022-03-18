package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

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
        val parameters = klass.primaryConstructor?.parameters
        val parameterValues = mutableMapOf<KParameter, Any?>()
        tokens.pop()
        while(tokens.current != OBJECT_END){
            val propertyName = tokens.popWordFinishedWith(COLON).trim()
            val filteredParameters = parameters?.filter { it.name == propertyName }
            if (filteredParameters != null && filteredParameters.isNotEmpty()) {
                parameterValues[filteredParameters[0]] =
                    parse(tokens, filteredParameters[0].type.classifier as KClass<*>)
                if(tokens.current == COMMA) tokens.pop()
            }
        }
        return klass.primaryConstructor?.callBy(parameterValues)
    }

}
