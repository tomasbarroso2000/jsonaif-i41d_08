package pt.isel

import pt.isel.setters.buildSetterFile
import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserDynamic : AbstractJsonParser() {

    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()

    override fun parsePrimitive(tokens: JsonTokens, klass: KClass<*>): Any? {
        val string = tokens.popWordPrimitive()
        return basicParser[klass]?.let { it(string) }
    }

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any {

        // Verifying if there are any constructors that take no parameters
        val isParameterless = klass
            .constructors
            .filter { it.visibility == KVisibility.PUBLIC }
            .any { it.parameters.all(KParameter::isOptional) }

        // Handle each case differently
        return if (isParameterless) parseObjectOptional(tokens, klass)
        else throw IllegalArgumentException()
    }

    /**
     * Parsing with optional properties
     */
    private fun parseObjectOptional(tokens: JsonTokens, klass: KClass<*>): Any {

        // Create initial instance
        val instance = klass.createInstance()

        // Create setter functions if they don't already exist
        setters.computeIfAbsent(klass, ::createSetterFiles)

        // Start of object
        tokens.pop(OBJECT_OPEN)

        // Obtain all properties until end of JSON object
        while (tokens.current != OBJECT_END) {
            parsePropertyOptional(klass, instance, tokens)
        }

        // End of object
        tokens.pop(OBJECT_END)
        return instance
    }

    private fun parsePropertyOptional(klass: KClass<*>, instance: Any?, tokens: JsonTokens) {

        // Property name from JSON Object
        val propertyName = tokens.popWordFinishedWith(COLON).trim()

        // Apply the new value of the property
        instance?.let { setters[klass]?.get(propertyName)?.apply(instance, tokens) }

        // Pop token content until it reaches another property or the end of object to prevent non-existing properties
        while (tokens.current != COMMA && tokens.current != OBJECT_END) tokens.pop()

        // If there are more properties it is necessary to pop the comma
        if (tokens.current == COMMA) tokens.pop(COMMA)
    }

    private fun createSetterFiles(klass: KClass<*>): Map<String, Setter> {

        // Map of setter functions
        val map = mutableMapOf<String, Setter>()

        // Create setter files if they don't already exist
        root.listFiles()?.let{ filesList ->
            filesList.filter { file -> file.name.startsWith("Setter${klass.simpleName}_") }
                .let { files ->
                    if (files.isNotEmpty())
                        files.forEach { file ->
                            val propertyName = file.name.split('_')[1]
                            map[propertyName] =
                                loadAndCreateInstance(file.nameWithoutExtension) as Setter
                        }
                }
        }

        // Build setter object
        klass
            .declaredMemberProperties
            .filter { it is KMutableProperty<*> }
            .map { it as KMutableProperty<*> }
            .forEach { prop -> buildSetterFile(map, klass, prop) }

        return map
    }

}
