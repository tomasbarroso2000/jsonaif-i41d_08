package pt.isel

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class JsonConvert(val converter: KClass<*> = EmptyConverter::class)

class EmptyConverter : Converter {
    override fun convert(str: String): Any {
        return str
    }
}
