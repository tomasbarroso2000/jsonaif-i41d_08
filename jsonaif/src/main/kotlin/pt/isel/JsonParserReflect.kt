package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties

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
        //Create initial instance
        val instance = klass
                        .constructors
                        .filter { it.visibility == KVisibility.PUBLIC }
                        .any { it.parameters.all(KParameter::isOptional) }
                        .let { if (it) klass.createInstance() else null }

        //Create setter functions if they don't already exist
        setters.computeIfAbsent(klass, ::createSetterFunctions)

        //Start of object
        tokens.pop(OBJECT_OPEN)

        //Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parseProperty(klass, instance, tokens)
        }

        //End of object
        tokens.pop(OBJECT_END)
        return instance
    }

    private fun createSetterFunctions(klass: KClass<*>) : Map<String, Setter> {
        val map = mutableMapOf<String, Setter>()
        klass
            .declaredMemberProperties
            .forEach { prop ->
                map[prop.name] = object : Setter {
                    override fun apply(target: Any, tokens: JsonTokens) {
                        val propertyValue = parse(tokens, prop.returnType.classifier as KClass<*>)
                        if (prop is KMutableProperty<*>)
                            prop.setter.call(target, propertyValue)
                    }
                }
            }
        return map
    }

    private fun parseProperty(klass: KClass<*>, instance: Any?, tokens: JsonTokens) {
        //Property name from JSON Object
        val propertyName = tokens.popWordFinishedWith(COLON).trim()

        //Apply the new value of the property
        instance?.let { setters[klass]?.get(propertyName)?.apply(instance, tokens) }

        //Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        //If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

}
