package pt.isel.setters

import pt.isel.JsonParserReflect
import pt.isel.JsonTokens
import pt.isel.Setter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty

class SetterWithoutConverter(private val property: KMutableProperty<*>): Setter {

    // Handle property KClass because it works differently with collections
    private val propertyKlass = property.returnType.let { returnType ->
        val arguments = returnType.arguments
        if (arguments.isNotEmpty()) arguments[0].type?.classifier as KClass<*>
        else returnType.classifier as KClass<*>
    }

    override fun apply(target: Any, tokens: JsonTokens) {

        // Manually convert instead of using parse when there is a converter
        val propertyValue = JsonParserReflect.parse(tokens, propertyKlass)

        // Setting the value of the property to the target instance
        property.setter.call(target, propertyValue)
    }
}