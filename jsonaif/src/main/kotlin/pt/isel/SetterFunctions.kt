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

fun getSetterClassName(klass: KClass<*>, propertyName: String) = "Setter${klass.simpleName}_$propertyName"

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
        val propertyKlass = property.returnType.let { returnType->
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

fun buildSetterFile(javaFiles: MutableList<JavaFile>, klass: KClass<*>, property: KMutableProperty<*>) {
    val propertyName = property.name
    val jsonProperty = property.findAnnotation<JsonProperty>()?.readAs
    val className = getSetterClassName(klass, jsonProperty ?: propertyName)

    // Handle converter if there is one
    val converter = property.findAnnotation<JsonConvert>()?.converter

    // Create setter classes
    val returnType = property.returnType
    val propertyType = (returnType.classifier as KClass<*>).java

    val arguments = returnType.arguments
    val elementType = if (arguments.isNotEmpty())
        (arguments[0].type?.classifier as KClass<*>).java
    else
        propertyType

    /**
    public class SetterStudentAlternative_birth implements Setter {
        private final Converter converter = new ConverterDate()
        public void apply(Object target, JsonTokens tokens) {
            tokens.pop('"');
            String readValue = tokens.popWordFinishedWith('"');
            Date v = (Date) new ConverterDate().convert(readValue);
            ((StudentAlternative) target).setBirth(v);
        }
    }
     */
    val converterField =
        converter?.let {
            FieldSpec.builder(Converter::class.java, "converter")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", it.java)
                .build()
        }

    val apply = MethodSpec.methodBuilder("apply")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(Any::class.java, "target")
        .addParameter(JsonTokens::class.java, "tokens")
        .let {
            if(converter != null) {
                it.addStatement("tokens.pop('\$L')", DOUBLE_QUOTES)
                it.addStatement("String readValue = tokens.popWordFinishedWith('\$L')", DOUBLE_QUOTES)
                it.addStatement("\$T v = (\$T) \$N.convert(readValue)",
                    propertyType,
                    propertyType,
                    converterField
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
        .addStatement(
            "((\$T) target).set${propertyName.replaceFirstChar { it.uppercase() }}(v)",
            klass.java
        )
        .build()

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

    javaFiles.add(JavaFile.builder("", newClass).build())
}