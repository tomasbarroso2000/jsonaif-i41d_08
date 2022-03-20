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

        val propertiesNames = setters[klass]?.keys

        //Start of object
        tokens.pop(OBJECT_OPEN)

        //Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            //Property name from JSON Object
            val propertyName = tokens.popWordFinishedWith(COLON).trim()

            if (propertiesNames == null || !propertiesNames.contains(propertyName)) {
                val property = klass.declaredMemberProperties.find { it.name == propertyName }
                //Set the property's value
                property?.let {

                    //If there is setter record
                    if (setters[klass] == null) setters[klass] = mutableMapOf()
                    val propertyMap = setters[klass]

                    propertyMap?.let {
                        if (propertyMap is MutableMap && propertyMap[propertyName] == null)
                            propertyMap[propertyName] = createSetter(property)
                    }
                }
            }
            instance?.let { setters[klass]?.get(propertyName)?.apply(instance, tokens) }

            //Find if property exists in KClass constructor

            //Pop token content until it reaches another property or the end of object
            while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

            //If there are more properties it is necessary to pop the comma
            if (tokens.current == COMMA) tokens.pop(COMMA)
        }

        //End of object
        tokens.pop(OBJECT_END)
        return instance
    }

    private fun createSetter(property: KProperty<*>): Setter {
        return object : Setter {
            override fun apply(target: Any, tokens: JsonTokens) {
                val propertyValue = parse(tokens, property.returnType.classifier as KClass<*>)
                if (property is KMutableProperty<*>)
                    property.setter.call(target, propertyValue)
            }
        }
    }

}
