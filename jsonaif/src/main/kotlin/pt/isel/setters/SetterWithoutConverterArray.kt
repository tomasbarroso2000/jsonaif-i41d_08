package pt.isel.setters

import pt.isel.JsonParserReflect
import pt.isel.JsonTokens
import pt.isel.Setter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty

class SetterWithoutConverterArray(private val property: KMutableProperty<*>, private val elementType: KClass<*>): Setter {
    override fun apply(target: Any, tokens: JsonTokens) {

        val propertyValue = JsonParserReflect.parseArray(tokens, elementType)

        // Setting the value of the property to the target instance
        property.setter.call(target, propertyValue)
    }
}