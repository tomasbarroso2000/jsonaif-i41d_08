package pt.isel

import com.squareup.javapoet.*
import java.io.File
import java.lang.reflect.Type
import java.net.URLClassLoader
import java.util.*
import javax.lang.model.element.Modifier
import javax.tools.ToolProvider
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaType


private val root = File("./build")
private val classLoader = URLClassLoader.newInstance(arrayOf(root.toURI().toURL()))
private val compiler = ToolProvider.getSystemJavaCompiler()

fun createSource(className: String): JavaFile {
    val main = MethodSpec.methodBuilder("apply")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(Void.TYPE)
        .addParameter(Array<String>::class.java, "args")
        .addStatement("\$T.out.println(\$S)", System::class.java, "Hello, JavaPoet!")
        .build()

    val constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .build()

    val newClass = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(constructor)
        .addMethod(main)
        .build()

    return JavaFile.builder("", newClass)
        .build()
}

fun createSetters(klass: KClass<*>): List<JavaFile> {
    val list = mutableListOf<JavaFile>()
    klass
        .declaredMemberProperties
        .filter { it is KMutableProperty<*> }
        .map { it as KMutableProperty<*> }
        .forEach { prop ->
            val propertyName = prop.findAnnotation<JsonProperty>()?.readAs ?: prop.name
            val className = "Setter${klass.simpleName}_$propertyName"

            // Handle converter if there is one
            val converter = prop.findAnnotation<JsonConvert>()?.converter
            val propertyValueConverter = converter?.let {
                val c = it.createInstance() as Converter
                c::convert
            }

            // Create setter classes
            val returnType = prop.returnType
            val arguments = returnType.arguments
            val propertyType =
                if (arguments.isNotEmpty())
                    (arguments[0].type?.classifier as KClass<*>).java.simpleName
                else
                    (returnType.classifier as KClass<*>).java.simpleName

            /*
            Not checking this
            val propertyValue =
                        if (propertyValueConverter != null) {
                            tokens.pop(DOUBLE_QUOTES)
                            val readValue = tokens.popWordFinishedWith(DOUBLE_QUOTES)
                            propertyValueConverter.call(readValue)
                        } else JsonParserDynamic.parse(tokens, propertyKlass)
             */
            val apply = MethodSpec.methodBuilder("apply")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Void.TYPE)
                .addParameter(klass.java, "target")
                .addParameter(JsonTokens::class.java, "tokens")
                .addStatement(
                    "$propertyType v = pt.isel.JsonParserDynamic.INSTANCE.parse(tokens, JvmClassMappingKt.getKotlinClass($propertyType.class))"
                )
                .addStatement(
                    "target.${getSetterName(propertyName)}(v)"
                )
                .build()

            val newClass = TypeSpec.classBuilder(className)
                .addSuperinterface(Setter::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addMethod(apply)
                .build()

            list.add(JavaFile.builder("", newClass).build())
        }
    return list
}

fun getSetterName(propertyName: String) =
    "set${propertyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"

fun loadAndCreateInstance(source: JavaFile): Any {
    // Save source in .java file.
    source.writeToFile(root)

    println(source.typeSpec.name)
    // Compile source file.
    compiler.run(null, null, null, "${root.path}/${source.typeSpec.name}.java")

    // Load and instantiate compiled class.
    return classLoader
        .loadClass(source.typeSpec.name)
        .getDeclaredConstructor()
        .newInstance()
}

data class StudentPoet (
    var nr: Int = 0,
    var name: String? = null
)

fun main() {
    val setters = createSetters(StudentPoet::class)
    setters.forEach {
        loadAndCreateInstance(it)
    }
}