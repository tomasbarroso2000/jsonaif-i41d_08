package pt.isel

import com.squareup.javapoet.JavaFile
import java.io.File
import java.net.URLClassLoader
import javax.tools.ToolProvider

val root = File("./build")
private val classLoader = URLClassLoader.newInstance(arrayOf(root.toURI().toURL()))
private val compiler = ToolProvider.getSystemJavaCompiler()

fun loadAndCreateInstance(source: JavaFile): Any {
    // Save source in .java file.
    source.writeToFile(root)

    // Compile source file.
    compiler.run(null, null, null, "${root.path}/${source.typeSpec.name}.java")

    // Load and instantiate compiled class.
    return classLoader
        .loadClass(source.typeSpec.name)
        .getDeclaredConstructor()
        .newInstance()
}

fun saveAndCompile(source: JavaFile): Any {
    // Save source in .java file.
    source.writeToFile(root)

    // Compile source file.
    compiler.run(null, null, null, "${root.path}/${source.typeSpec.name}.java")

    // Load and instantiate compiled class.
    return classLoader
        .loadClass(source.typeSpec.name)
        .getDeclaredConstructor()
        .newInstance()
}

fun loadAndCreateInstance(className: String): Any? {
    return loadClassIfExists(className)
        ?.getDeclaredConstructor()
        ?.newInstance()
}

private fun loadClassIfExists(className: String): Class<*>? {
    return try {
        classLoader
            .loadClass(className)
    } catch (e: ClassNotFoundException) {
        null
    }
}