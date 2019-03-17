package com.navid.loggergenerator

import com.navid.loggergenerator.config.MappingConfig
import com.navid.loggergenerator.config.MappingEntry
import com.navid.loggergenerator.config.SentenceEntry
import com.squareup.javapoet.*
import com.squareup.javapoet.MethodSpec.methodBuilder
import java.io.File
import javax.lang.model.element.Modifier


val saClassName = ClassName.get("net.logstash.logback.argument", "StructuredArguments")
val saClass = ClassName.get("net.logstash.logback.argument", "StructuredArgument")

typealias NamingStrategy = (SentenceEntry, MappingConfig) -> MethodSpec

fun generateJavaFile(mappingConfig: MappingConfig,
                     packageName: String,
                     className: String,
                     outputFolder: String,
                     namingStrategy: SentenceNamingStrategy,
                     javaCompat: JavaCompatibility) {

    val genClass = TypeSpec
            .classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

    if (javaCompat.ordinal > JavaCompatibility.JAVA7.ordinal) {
        generateConsumersJDK8(genClass)
    }


    mappingConfig.getMappings().forEach {
        genClass.addMethod(createKvFunction(it))
        genClass.addMethod(createListFunction(it))
        genClass.addMethod(createVarargFunction(it))
    }

    mappingConfig.getSentences().forEach {
        if (javaCompat.ordinal > JavaCompatibility.JAVA7.ordinal) {
            if (namingStrategy == SentenceNamingStrategy.BY_CODE) {
                genClass.addMethod(createSentencesByCode(it, mappingConfig))
                genClass.addMethod(createSentencesByCodeJDK8(it, mappingConfig))

            } else {
                genClass.addMethod(createSentencesBySentence(it, mappingConfig))
            }
        } else {
            if (namingStrategy == SentenceNamingStrategy.BY_CODE) {
                genClass.addMethod(createSentencesByCode(it, mappingConfig))
                genClass.addMethod(createSentencesByCodeJDK7(it, mappingConfig))
            } else {
                genClass.addMethod(createSentencesBySentence(it, mappingConfig))
            }
        }
    }

    mappingConfig.getContext().forEach {
        genClass.addMethod(createContext(it, mappingConfig))
    }

    mappingConfig.getContext().forEach {
        genClass.addMethod(deleteContext(it, mappingConfig))
    }

    genClass.addMethod(resetContext())

    val javaFile = JavaFile
            .builder(packageName, genClass.build())
            .addStaticImport(saClassName, "keyValue")
            .build()

    javaFile.writeTo(File(outputFolder))
}

private fun generateConsumersJDK8(genClass: TypeSpec.Builder) {
    genClass.addType(TypeSpec.interfaceBuilder("MonoConsumer")
            .addMethod(methodBuilder("accept")
                    .addParameter(getTypeName("String"), "var1")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build())

    genClass.addType(TypeSpec.interfaceBuilder("BiConsumer")
            .addMethod(methodBuilder("accept")
                    .addParameter(getTypeName("String"), "var1")
                    .addParameter(getTypeName("Object"), "var2")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build())

    genClass.addType(TypeSpec.interfaceBuilder("TriConsumer")
            .addMethod(methodBuilder("accept")
                    .addParameter(getTypeName("String"), "var1")
                    .addParameter(getTypeName("Object"), "var2")
                    .addParameter(getTypeName("Object"), "var3")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build())

    genClass.addType(TypeSpec.interfaceBuilder("ManyConsumer")
            .addMethod(methodBuilder("accept")
                    .addParameter(getTypeName("String"), "var1")
                    .addParameter(ArrayTypeName.of(getTypeName("Object")), "var2").varargs()
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build())
}


fun camelCase(string: String): String {
    return string.substring(0, 1).toUpperCase() + string.substring(1)
}

//boolean , byte , char , short , int , long , float and double
fun getTypeName(name: String): TypeName {
    return when (name) {
        "boolean" -> TypeName.BOOLEAN
        "byte" -> TypeName.BYTE
        "char" -> TypeName.CHAR
        "short" -> TypeName.SHORT
        "int" -> TypeName.INT
        "long" -> TypeName.LONG
        "float" -> TypeName.FLOAT
        "double" -> TypeName.DOUBLE
        else -> ClassName.bestGuess(name).box()
    }
}

fun createKvFunction(entry: MappingEntry): MethodSpec {
    val main = MethodSpec.methodBuilder(generateKvFunctionName(entry.getName()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(getTypeName(entry.getType()!!), entry.getName())
            .returns(saClass)
            .addStatement("return keyValue(\$S,\$L)", entry.getName(), entry.getName())
            .build()

    return main
}

private fun generateKvFunctionName(variableName: String) = "kv" + camelCase(variableName)

fun createContext(entry: String, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("setContext" + camelCase(entry))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    val mapped = config.getMappings().filter { m -> m.getName().equals(entry) }.first()
    builder.addParameter(getTypeName(mapped.getType()!!), mapped.getName())

    if ("java.lang.String" == mapped.getType()) {
        builder.addStatement("org.slf4j.MDC.put(\$S,\$L)", "ctx." + mapped.getName(), mapped.getName())
    } else {
        builder.addStatement("org.slf4j.MDC.put(\$S,String.valueOf(\$L))", "ctx." + mapped.getName(), mapped.getName())
    }

    return builder.build()
}

fun deleteContext(entry: String, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("removeContext" + camelCase(entry))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    val mapped = config.getMappings().filter { m -> m.getName().equals(entry) }.first()

    builder.addStatement("org.slf4j.MDC.remove(\$S)", "ctx." + mapped.getName())

    return builder.build()
}

fun resetContext(): MethodSpec {
    val builder = MethodSpec.methodBuilder("resetContext" )
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addStatement("org.slf4j.MDC.clear()")

    return builder.build()
}

fun createListFunction(entry: MappingEntry): MethodSpec {
    val iterableType = ParameterizedTypeName.get(ClassName.get(Iterable::class.java), getTypeName(entry.getType()!!).box())

    return MethodSpec
            .methodBuilder("a" + camelCase(entry.getName()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(iterableType, entry.getName())
            .returns(saClass)
            .addStatement("return new net.logstash.logback.marker.ObjectAppendingMarker(\$S,\$L)", entry.getName(), entry.getName())
            .build()
}

fun createVarargFunction(entry: MappingEntry): MethodSpec {
    return MethodSpec.methodBuilder("a" + camelCase(entry.getName()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ArrayTypeName.of(getTypeName(entry.getType()!!)), entry.getName()).varargs()
            .returns(saClass)
            .addStatement("return new net.logstash.logback.marker.ObjectAppendingMarker(\$S,\$L)", entry.getName(), entry.getName())
            .build()
}

fun createSentencesByCode(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.getCode()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")

    val sb1: StringBuilder = StringBuilder("logger." + entry.getDefaultLevel() + "(\"").append(entry.getMessage())

    entry.getVariables().forEach { v ->
        val mapped = config.getMappings().filter { m -> m.getName() == v }.first()
        builder.addParameter(getTypeName(mapped.getType()!!), mapped.getName())
        sb1.append(" {}")
    }

    entry.getExtradata().forEach{ v ->
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry))
    sb1.append(createExtradataString(entry, config))
    sb1.append(")")

    builder.addStatement(sb1.toString())

    return builder.build()
}

fun createSentencesByCodeJDK7(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.getCode()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")
    builder.addParameter(getTypeName("org.slf4j.event.Level"), "level")

    val sb1: StringBuilder = StringBuilder("(\"").append(entry.getMessage())

    entry.getVariables().forEach { v ->
        val mapped = config.getMappings().filter { m -> m.getName().equals(v) }.first()
        builder.addParameter(getTypeName(mapped.getType()!!), mapped.getName())
        sb1.append(" {}")
    }

    entry.getExtradata().forEach{ v ->
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry))
    sb1.append(createExtradataString(entry, config))
    sb1.append(")")

    builder.beginControlFlow("switch(level)")
    builder.addCode("case ERROR:\n")
    builder.addStatement("logger.error" + sb1.toString())
    builder.addStatement("break")
    builder.addCode("case WARN:\n")
    builder.addStatement("logger.warn" + sb1.toString())
    builder.addStatement("break")
    builder.addCode("case INFO:\n")
    builder.addStatement("logger.info" + sb1.toString())
    builder.addStatement("break")
    builder.addCode("case DEBUG:\n")
    builder.addStatement("logger.debug" + sb1.toString())
    builder.addStatement("break")
    builder.addCode("case TRACE:\n")
    builder.addStatement("logger.trace" + sb1.toString())
    builder.addStatement("break")
    builder.endControlFlow()


    return builder.build();
}

fun createSentencesByCodeJDK8(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.getCode()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    when (entry.getVariables().size + entry.getExtradata().size) {
        0 -> builder.addParameter(getTypeName("MonoConsumer"), "logger")
        1 -> builder.addParameter(getTypeName("BiConsumer"), "logger")
        2 -> builder.addParameter(getTypeName("TriConsumer"), "logger")
        else -> builder.addParameter(getTypeName("ManyConsumer"), "logger")
    }

    val sb1: StringBuilder = StringBuilder("logger.accept(\"").append(entry.getMessage())

    entry.getVariables().forEach { v ->
        val mapped = config.getMappings().filter { m -> m.getName().equals(v) }.first()
        builder.addParameter(getTypeName(mapped.getType()!!), mapped.getName())
        sb1.append(" {}")
    }

    entry.getExtradata().forEach{ v ->
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry))
    sb1.append(createExtradataString(entry, config))
    sb1.append(")")

    builder.addStatement(sb1.toString())

    return builder.build();
}


fun createSentencesByCodeWithLevel(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.getCode()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")
    val paramsArray: Array<Object> = Array(entry.getVariables().size) { i -> entry.getVariables()[i] as Object }
    val sb1: StringBuilder = StringBuilder("logger.info(\"").append(entry.getMessage())

    entry.getVariables().forEach { v ->
        val mapped = config.getMappings().filter { m -> m.getName().equals(v) }.first()
        builder.addParameter(getTypeName(mapped.getType()!!), mapped.getName())
        sb1.append(" {}")
    }

    sb1.append("\"").append(createDollarString(entry)).append(")")

    builder.addStatement(sb1.toString(), *paramsArray)

    return builder.build();
}


fun createDollarString(entry: SentenceEntry): String {
    return if (entry.getVariables().isEmpty()) {
        ""
    } else {
        entry.getVariables().joinToString(",", prefix = ",")
    }

}

fun createFunctionString(entry: SentenceEntry): String {
    return if (entry.getVariables().isEmpty()) {
        ""
    } else {
        entry.getVariables().map { s -> generateKvFunctionName(s) + "(" + s + ")" }.joinToString(",", prefix = ",")
    }
}

fun createExtradataString(entry: SentenceEntry, config: MappingConfig): String {
    return if (entry.getExtradata().isEmpty()) {
        ""
    } else {

        entry.getExtradata().map {
            s ->
            var mapping = config.getMappings().stream().filter{m -> m.getName()!! == s.key}.findFirst().get()
            when(mapping.getType()){
                "java.lang.String" -> generateKvFunctionName(s.key) + "(\"" + s.value + "\")"
                "java.lang.Long","long" -> generateKvFunctionName(s.key) + "(" + s.value + "L)"
                "java.lang.Float","float" -> generateKvFunctionName(s.key) + "(" + s.value + "f)"

                else ->  generateKvFunctionName(s.key) + "(" + s.value + ")"
        }
        }.joinToString(",", prefix = ",")
    }
}

fun createSentencesBySentence(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    return MethodSpec.methodBuilder("audit" + camelCase(entry.getCode()!!))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .build()
}