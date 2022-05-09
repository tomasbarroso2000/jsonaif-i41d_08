package pt.isel.setters

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import pt.isel.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

// Build the class name for the setter class
private fun buildSetterClassName(className: String?, propertyName: String) = "Setter${className}_$propertyName"

// Map to parse primitives faster
val parserMap: Map<KClass<*>, String> = mapOf(
    Byte::class to "Byte.parseByte",
    Short::class to "Short.parseShort",
    Int::class to "Integer.parseInt",
    Long::class to "Long.parseLong",
    Float::class to "Float.parseFloat",
    Double::class to "Double.parseDouble",
    Boolean::class to "Boolean.parseBoolean",
    Char::class to "Char.parseChar"
)

fun addPropertySetter(mapOfSetters: MutableMap<String, Setter>, property: KMutableProperty<*>) {
    val propertyName = property.findAnnotation<JsonProperty>()?.readAs ?: property.name

    // Handle converter if there is one
    val converter = property.findAnnotation<JsonConvert>()?.converter
    val propertyValueConverter = converter?.let {
        it.createInstance() as Converter
    }

    // Create Setter object
    if (propertyValueConverter != null)
        mapOfSetters[propertyName] = SetterWithConverter(property, propertyValueConverter)
    else
        mapOfSetters[propertyName] = SetterWithoutConverter(property)
}

fun buildSetterFile(mapOfSetters: MutableMap<String, Setter>, klass: KClass<*>, property: KMutableProperty<*>) {
    val propertyName = property.name
    val jsonProperty = property.findAnnotation<JsonProperty>()?.readAs
    val className = buildSetterClassName(klass.simpleName, jsonProperty ?: propertyName)

    // Handle converter if there is one
    val converter = property.findAnnotation<JsonConvert>()?.converter

    // Create setter classes
    val returnType = property.returnType
    val propertyType = (returnType.classifier as KClass<*>).java

    // Define the element type
    val arguments = returnType.arguments
    val elementType = if (arguments.isNotEmpty())
        (arguments[0].type?.classifier as KClass<*>).java
    else
        propertyType

    // Build the converter field if necessary
    val converterField =
        converter?.let {
            FieldSpec.builder(Converter::class.java, "converter")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", it.java)
                .build()
        }

    // Build the apply method of the Setter interface
    val apply = MethodSpec.methodBuilder("apply")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(Any::class.java, "target")
        .addParameter(JsonTokens::class.java, "tokens")
        .let {
            // If there is a converter apply it to the readValue
            if (converter != null) {
                it.addStatement("tokens.pop('\$L')", DOUBLE_QUOTES)
                it.addStatement("String readValue = tokens.popWordFinishedWith('\$L')", DOUBLE_QUOTES)
                it.addStatement(
                    "\$T v = (\$T) \$N.convert(readValue)",
                    propertyType,
                    propertyType,
                    converterField
                )
            // If there is no converter parse it normally
            } else {
                val primitiveParser = parserMap[propertyType.kotlin]
                if (primitiveParser != null) {
                    it.addStatement("String readValue = tokens.popWordPrimitive().trim()")
                    it.addStatement(
                        "\$T v = \$L(readValue)",
                        propertyType,
                        primitiveParser
                    )
                } else {
                    it.addStatement(
                        "\$T v = (\$T) \$T.INSTANCE.parse(tokens, kotlin.jvm.JvmClassMappingKt.getKotlinClass(\$T.class))",
                        propertyType,
                        propertyType,
                        JsonParserDynamic::class.java,
                        elementType
                    )
                }
            }
        }
        // Call the property setter
        .addStatement(
            "((\$T) target).\$L(v)",
            klass.java,
            property.setter.javaMethod?.name
        )
        .build()

    // Build the new class with the converter field (if there is one) and the apply method built before
    val newClass = TypeSpec.classBuilder(className)
        .addSuperinterface(Setter::class.java)
        .addModifiers(Modifier.PUBLIC)
        .let {
            if (converterField != null)
               it.addField(converterField)
            it
        }
        .addMethod(apply)
        .build()

    val javaFile = JavaFile.builder("", newClass).build()

    // Add an instance of the created class to the map of setters
    mapOfSetters[jsonProperty ?: propertyName] = loadAndCreateInstance(javaFile) as Setter
}