package pt.isel.setters

import pt.isel.JsonParserReflect
import pt.isel.JsonTokens
import pt.isel.Setter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty

class SetterWithoutConverter(private val property: KMutableProperty<*>): Setter {

    // Obtain property KClass
    private val propertyKlass = property.returnType.classifier as KClass<*>

    override fun apply(target: Any, tokens: JsonTokens) {

        val propertyValue = JsonParserReflect.parse(tokens, propertyKlass)

        // Setting the value of the property to the target instance
        property.setter.call(target, propertyValue)
    }
}