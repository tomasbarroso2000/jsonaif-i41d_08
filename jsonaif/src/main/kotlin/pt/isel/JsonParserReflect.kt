package pt.isel

import pt.isel.setters.addPropertySetter
import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserReflect : AbstractJsonParser() {

    /**
     * For each domain class we keep a Map<String, Setter> relating properties names with their setters.
     * This is for Part 2 of Jsonaif workout.
     */
    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()

    /**
     * Used when some properties need to be used directly in the constructor.
     * Keeps a Map<KParameter, KClass<*>> relating constructor parameters to their KClass
     */
    private val constructorParameters = mutableMapOf<KClass<*>, Map<KParameter, KClass<*>>>()

    override fun <T : Any> parsePrimitive(tokens: JsonTokens, klass: KClass<T>): T {
        val string = tokens.popWordPrimitive().trim()
        return basicParser[klass]?.let { it(string) } as T
    }

    override fun <T : Any> parseObject(tokens: JsonTokens, klass: KClass<T>): T? {

        // Verifying if there are any constructors that take no parameters
        val isParameterless = klass
            .constructors
            .filter { it.visibility == KVisibility.PUBLIC }
            .any { it.parameters.all(KParameter::isOptional) }

        // Handle each case differently
        return if (isParameterless)  parseObjectOptional(tokens, klass)
        else parseObjectNotOptional(tokens, klass)
    }

    /**
     * Parsing with optional properties
     */

    private fun <T: Any> parseObjectOptional(tokens: JsonTokens, klass: KClass<T>): T {

        // Create initial instance
        val instance = klass.createInstance()

        // Create setter functions if they don't already exist
        setters.computeIfAbsent(klass, ::createSetterFunctions)

        // Start of object
        tokens.pop(OBJECT_OPEN)

        // Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parsePropertyOptional(klass, instance, tokens)
        }

        // End of object
        tokens.pop(OBJECT_END)
        return instance
    }

    private fun parsePropertyOptional(klass: KClass<*>, instance: Any?, tokens: JsonTokens) {

        // Property name from JSON Object
        val propertyName = tokens.popWordFinishedWith(COLON).trim()

        // Apply the new value of the property
        instance?.let { setters[klass]?.get(propertyName)?.apply(instance, tokens) }

        // Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        // If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

    private fun createSetterFunctions(klass: KClass<*>) : Map<String, Setter> {

        // Map of setter functions
        val map = mutableMapOf<String, Setter>()

        // Build setter object
        klass
            .declaredMemberProperties
            .filter { it is KMutableProperty<*> }
            .map { it as KMutableProperty<*> }
            .forEach { prop -> addPropertySetter(map, prop) }
        return map
    }

    /**
     * Parsing without optional properties
     */

    private fun <T: Any> parseObjectNotOptional(tokens: JsonTokens, klass: KClass<T>): T? {

        // The constructor that will be called once the parameters are ready
        val constructor = klass.primaryConstructor

        // Create constructor parameters map if it doesn't already exist
        constructorParameters.computeIfAbsent(klass, ::createConstructorParameters)

        // Map associating the KClass constructor parameters with their respective value
        val parameterValues = mutableMapOf<KParameter, Any?>()

        // Start of object
        tokens.pop(OBJECT_OPEN)

        // Obtains all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parsePropertyNotOptional(klass, parameterValues, tokens)
        }

        // End of object
        tokens.pop(OBJECT_END)
        return try {
            constructor?.callBy(parameterValues)
        } catch (e: Exception) {
            null
        }
    }

    private fun  parsePropertyNotOptional(
        klass: KClass<*>,
        parameterValues: MutableMap<KParameter, Any?>,
        tokens: JsonTokens)
    {
        // Property name from JSON Object
        val jsonPropertyName = tokens.popWordFinishedWith(COLON).trim()

        // The parameters of the constructor
        val parameters = constructorParameters[klass]?.keys


        // Find if property exists in constructor
        val parameter = parameters?.find { prop->
            val propertyName = prop.findAnnotation<JsonProperty>()?.readAs ?: prop.name
            propertyName == jsonPropertyName
        }

        // KClass of the parameter
        val parameterKlass = constructorParameters[klass]?.get(parameter)

        // Handle converter if there is one
        val converter = parameter?.findAnnotation<JsonConvert>()?.converter
        val propertyValueConverter = converter?.let {
            val c = it.createInstance() as Converter
            c::convert
        }

        // Save the property and its value in parameterValues Map
        parameter?.let {
            if (parameterKlass != null) {
                parameterValues[parameter] =
                    if (propertyValueConverter != null) {
                        tokens.pop(DOUBLE_QUOTES)
                        val readValue = tokens.popWordFinishedWith(DOUBLE_QUOTES)
                        propertyValueConverter.call(readValue)
                    } else {
                        if (it.type.arguments.isEmpty())
                            parse(tokens, parameterKlass)
                        else
                            parseArray(tokens, parameterKlass)
                    }
            }
        }

        // Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        // If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

    private fun createConstructorParameters(klass: KClass<*>) : Map<KParameter, KClass<*>> {

        // Map of constructor parameters
        val map = mutableMapOf<KParameter, KClass<*>>()

        val constructor = klass.primaryConstructor

        // Insert parameters in the map
        constructor?.parameters?.forEach { parameter ->
            val elementType = parameter.type.let { type ->
                val arguments = type.arguments
                if (arguments.isNotEmpty()) arguments[0].type?.classifier as KClass<*>
                else type.classifier as KClass<*>
            }
            map[parameter] = elementType
        }
        return map
    }
}
