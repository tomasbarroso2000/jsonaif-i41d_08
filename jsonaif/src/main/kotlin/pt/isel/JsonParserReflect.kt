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

        //Get properties from klass
        val properties = klass.declaredMemberProperties

        //Start of object
        tokens.pop(OBJECT_OPEN)

        //Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            //Property name from JSON Object
            val propertyName = tokens.popWordFinishedWith(COLON).trim()

            //Find if property exists in KClass constructor
            val property = properties.find { it.name == propertyName }

            //Set the property's value
            property?.let {
                val propertyValue = parse(tokens, property.returnType.classifier as KClass<*>)
                if (property is KMutableProperty<*>)
                    property.setter.call(instance, propertyValue)
            }

            //Pop token content until it reaches another property or the end of object
            while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

            //If there are more properties it is necessary to pop the comma
            if (tokens.current == COMMA) tokens.pop(COMMA)
        }

        //End of object
        tokens.pop(OBJECT_END)
        return instance
    }

}
