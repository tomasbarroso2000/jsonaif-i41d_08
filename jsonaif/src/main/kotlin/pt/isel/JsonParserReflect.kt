package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserReflect  : AbstractJsonParser() {

    /**
     * For each domain class we keep a Map<String, Setter> relating properties names with their setters.
     * This is for Part 2 of Jsonaif workout.
     */
    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()
    private val constructorParameters = mutableMapOf<KClass<*>, Map<KParameter, KClass<*>>>()
    
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

        val constructor = klass.primaryConstructor
        constructorParameters.computeIfAbsent(klass, ::createConstructorParameters)

        //Map associating the KClass constructor parameters with its respective value
        val parameterValues = mutableMapOf<KParameter, Any?>()

        //Start of object
        tokens.pop(OBJECT_OPEN)

        //Obtains all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parsePropertyNotOptional(klass, parameterValues, tokens)
        }

        //end of object
        tokens.pop(OBJECT_END)
        return constructor?.callBy(parameterValues)
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

    private fun  parsePropertyNotOptional(
        klass: KClass<*>,
        parameterValues: MutableMap<KParameter, Any?>,
        tokens: JsonTokens)
    {
        //Property name from JSON Object
        val jsonPropertyName = tokens.popWordFinishedWith(COLON).trim()
        val parameters = constructorParameters[klass]?.keys


        //Find if property exists in KClass constructor
        val parameter = parameters?.find { prop->
            val propertyName = prop.findAnnotation<JsonProperty>()?.readAs ?: prop.name
            propertyName == jsonPropertyName
        }
        val parameterKlass = constructorParameters[klass]?.get(parameter)

        //Save the property and its value in parameterValues Map
        parameter?.let {
            if (parameterKlass != null) {
                parameterValues[parameter] =
                    parse(tokens, parameterKlass)
            }
        }

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
                val propertyName = prop.findAnnotation<JsonProperty>()?.readAs ?: prop.name
                val convertKlass = prop.findAnnotation<JsonConvert>()?.convertTo
                val propertyValueConverter =
                    convertKlass
                        ?.let { klass ->
                            klass.declaredFunctions.find { it.name == "convert" }
                        }
                map[propertyName] = object : Setter {

                    val propertyKlass = prop.returnType.let { returnType->
                        val arguments = returnType.arguments
                        if (arguments.isNotEmpty()) arguments[0].type?.classifier as KClass<*>
                        else returnType.classifier as KClass<*>
                    }

                    override fun apply(target: Any, tokens: JsonTokens) {
                        val propertyValue =
                            if (propertyValueConverter != null) {
                                tokens.pop(DOUBLE_QUOTES)
                                val readValue = tokens.popWordFinishedWith(DOUBLE_QUOTES)
                                propertyValueConverter.call(convertKlass.createInstance(), readValue)
                            } else parse(tokens, propertyKlass)
                        prop.setter.call(target, propertyValue)
                    }
                }
            }
        return map
    }

    private fun createConstructorParameters(klass: KClass<*>) : Map<KParameter, KClass<*>> {
        val map = mutableMapOf<KParameter, KClass<*>>()
        val constructor = klass.primaryConstructor
        constructor?.parameters?.forEach { map[it] = it.type.classifier as KClass<*> }
        return map
    }
}
