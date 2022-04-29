package pt.isel.setters

import pt.isel.Converter
import pt.isel.DOUBLE_QUOTES
import pt.isel.JsonTokens
import pt.isel.Setter
import kotlin.reflect.KMutableProperty

class SetterWithConverter(private val property: KMutableProperty<*>, private val converter: Converter) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        // Convert instead of using parse
        tokens.pop(DOUBLE_QUOTES)
        val readValue = tokens.popWordFinishedWith(DOUBLE_QUOTES)
        val propertyValue = converter.convert(readValue)

        // Setting the value of the property to the target instance
        property.setter.call(target, propertyValue)
    }
}