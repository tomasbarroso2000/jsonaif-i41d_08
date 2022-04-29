package pt.isel

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

// Build the class name for the setter class
fun getSetterClassName(className: String?, propertyName: String) = "Setter${className}_$propertyName"

fun addPropertySetter(mapOfSetters: MutableMap<String, Setter>, property: KMutableProperty<*>) {
    val propertyName = property.findAnnotation<JsonProperty>()?.readAs ?: property.name

    // Handle converter if there is one
    val converter = property.findAnnotation<JsonConvert>()?.converter
    val propertyValueConverter = converter?.let {
        val c = it.createInstance() as Converter
        c::convert
    }

    // Create Setter object
    mapOfSetters[propertyName] = object : Setter {

        // Handle property KClass because it works differently with collections
        val propertyKlass = property.returnType.let { returnType ->
            val arguments = returnType.arguments
            if (arguments.isNotEmpty()) arguments[0].type?.classifier as KClass<*>
            else returnType.classifier as KClass<*>
        }

        override fun apply(target: Any, tokens: JsonTokens) {

            // Manually convert instead of using parse when there is a converter
            val propertyValue =
                if (propertyValueConverter != null) {
                    tokens.pop(DOUBLE_QUOTES)
                    val readValue = tokens.popWordFinishedWith(DOUBLE_QUOTES)
                    propertyValueConverter.call(readValue)
                } else JsonParserReflect.parse(tokens, propertyKlass)

            // Setting the value of the property to the target instance
            property.setter.call(target, propertyValue)
        }
    }
}

fun buildSetterFile(mapOfSetters: MutableMap<String, Setter> , klass: KClass<*>, property: KMutableProperty<*>) {
    val propertyName = property.name
    val jsonProperty = property.findAnnotation<JsonProperty>()?.readAs
    val className = getSetterClassName(klass.simpleName, jsonProperty ?: propertyName)

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
            if(converter != null) {
                it.addStatement("tokens.pop('\$L')", DOUBLE_QUOTES)
                it.addStatement("String readValue = tokens.popWordFinishedWith('\$L')", DOUBLE_QUOTES)
                it.addStatement("\$T v = (\$T) \$N.convert(readValue)",
                    propertyType,
                    propertyType,
                    converterField
                )
            // If there is no converter parse it normally
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
        // Call the property setter
        .addStatement(
            "((\$T) target).set${propertyName.replaceFirstChar { it.uppercase() }}(v)",
            klass.java
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