package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
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
        val isParameterless = klass
            .constructors
            .filter { it.visibility == KVisibility.PUBLIC }
            .any { it.parameters.all(KParameter::isOptional) }

        return if (isParameterless)  parseObjectWithOptionalParameters(tokens, klass)
        else parseObjectWithoutOptionalParameters(tokens, klass)
    }

    private fun parseObjectWithOptionalParameters(tokens: JsonTokens, klass: KClass<*>): Any {

        //Create initial instance
        val instance = klass.createInstance()

        //Create setter functions if they don't already exist
        setters.computeIfAbsent(klass, ::createSetterFunctions)

        //Start of object
        tokens.pop(OBJECT_OPEN)

        //Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parsePropertyOptional(klass, instance, tokens)
        }

        //End of object
        tokens.pop(OBJECT_END)
        return instance
    }

    private fun parseObjectWithoutOptionalParameters(tokens: JsonTokens, klass: KClass<*>): Any? {
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
                parsePropertyNotOptional(parameters, parameterValues, tokens)
            }

            //end of object
            tokens.pop(OBJECT_END)
            return constructor?.callBy(parameterValues)
    }

    private fun  parsePropertyNotOptional(
        parameters: List<KParameter>?,
        parameterValues: MutableMap<KParameter, Any?>,
        tokens: JsonTokens)
    {
        //Property name from JSON Object
        val propertyName = tokens.popWordFinishedWith(COLON).trim()

        //Find if property exists in KClass constructor
        val parameter = parameters?.find { it.name == propertyName }

        //Save the property and it`s value in parameterValues Map
        parameter?.let {
            parameterValues[parameter] =
                parse(tokens, parameter.type.classifier as KClass<*>)
        }

        //Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        //If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

    private fun parsePropertyOptional(klass: KClass<*>, instance: Any?, tokens: JsonTokens) {
        //Property name from JSON Object
        val propertyName = tokens.popWordFinishedWith(COLON).trim()

        //Apply the new value of the property
        instance?.let { setters[klass]?.get(propertyName)?.apply(instance, tokens) }

        //Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        //If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

    private fun createSetterFunctions(klass: KClass<*>) : Map<String, Setter> {
        val map = mutableMapOf<String, Setter>()
        klass
            .declaredMemberProperties
            .filter { it is KMutableProperty<*> }
            .map { it as KMutableProperty<*> }
            .forEach { prop ->
                map[prop.name] = object : Setter {
                    val propertyKlass = prop.returnType.classifier as KClass<*>
                    override fun apply(target: Any, tokens: JsonTokens) {
                        println(propertyKlass.qualifiedName)
                        val propertyValue = parse(tokens, propertyKlass)
                        prop.setter.call(target, propertyValue)
                    }
                }
            }
        return map
    }
}
