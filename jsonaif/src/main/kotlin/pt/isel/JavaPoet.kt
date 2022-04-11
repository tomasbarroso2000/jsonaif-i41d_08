package pt.isel

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.net.URLClassLoader
import javax.lang.model.element.Modifier
import javax.tools.ToolProvider


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

    val helloWorld = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethod(main)
        .build()

    return JavaFile.builder("javapoet", helloWorld)
        .build()
}

fun loadAndCreateInstance(source: JavaFile): Any {
    // Save source in .java file.
    source.writeToFile(root)

    println(source.typeSpec.name)
    // Compile source file.
    compiler.run(null, null, null, "${root.path}/javapoet/${source.typeSpec.name}.java")

    // Load and instantiate compiled class.
    return classLoader
        .loadClass(source.typeSpec.name)
        .getDeclaredConstructor()
        .newInstance()
}

fun main() {
    loadAndCreateInstance(createSource("yes"))
}