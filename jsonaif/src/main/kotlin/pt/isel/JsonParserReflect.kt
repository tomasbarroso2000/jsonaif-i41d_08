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
        //The constructor of the KClass passed as parameter
        val constructor = klass.primaryConstructor
        //List containing all the parameters in the KClass constructor
        val parameters = constructor?.parameters
        //Map associating the KClass constructor parameters with its respective value
        val parameterValues = mutableMapOf<KParameter, Any?>()
        //Start of object
        tokens.pop(OBJECT_OPEN)
        //Obtains all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            //Property name from JSON Object
            val propertyName = tokens.popWordFinishedWith(COLON).trim()
            //Find if property exists in KClass constructor
            val parameter = parameters?.find { it.name == propertyName }
            //Save the property and it`s value in parameterValues Map
            parameter?.let {
                parameterValues[parameter] =
                    parse(tokens, parameter.type.classifier as KClass<*>)
            }
            //Pop token content until it reaches another property or the end of object
            while (
                tokens.current != COMMA &&
                tokens.current != OBJECT_END
            ) tokens.pop()
            //In case of existing another property it is necessary to pop the comma
            if (tokens.current == COMMA) tokens.pop(COMMA)
        }
        //end of object
        tokens.pop(OBJECT_END)
        return constructor?.callBy(parameterValues)
    }

}
