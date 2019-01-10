package com.navid.loggergenerator

import com.squareup.javapoet.*
import java.io.File
import javax.lang.model.element.Modifier
import com.squareup.javapoet.MethodSpec.methodBuilder
import javax.lang.model.type.PrimitiveType


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

    validateMappingConfig(mappingConfig)


    if (javaCompat.ordinal > JavaCompatibility.JAVA7.ordinal) {
        generateConsumersJDK8(genClass)
    }


    mappingConfig.mappings.forEach{
        genClass.addMethod(createKvFunction(it))
        genClass.addMethod(createListFunction(it))
        genClass.addMethod(createVarargFunction(it))
    }

    mappingConfig.sentences.forEach {
        if (javaCompat.ordinal > JavaCompatibility.JAVA7.ordinal) {
            if(namingStrategy == SentenceNamingStrategy.BY_CODE ) {
                genClass.addMethod(createSentencesByCode(it, mappingConfig))
                genClass.addMethod(createSentencesByCodeJDK8(it, mappingConfig))

            } else {
                genClass.addMethod(createSentencesBySentence(it, mappingConfig))
            }
        } else {
            if(namingStrategy == SentenceNamingStrategy.BY_CODE ) {
                genClass.addMethod(createSentencesByCode(it, mappingConfig))
                genClass.addMethod(createSentencesByCodeJDK7(it, mappingConfig))
            } else {
                genClass.addMethod(createSentencesBySentence(it, mappingConfig))
            }
        }
    }

    val javaFile = JavaFile
            .builder(packageName, genClass.build())
            .addStaticImport(saClassName, "kv")
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

fun validateMappingConfig(mappingConfig: MappingConfig) {
//logger is a reserved word
}

fun camelCase(string: String): String {
    return string.substring(0,1).toUpperCase() + string.substring(1)
}
//boolean , byte , char , short , int , long , float and double
fun getTypeName(name : String) : TypeName {
    return when(name) {
        "boolean" -> TypeName.BOOLEAN
        "byte" -> TypeName.BYTE
        "char" -> TypeName.CHAR
        "short" -> TypeName.SHORT
        "int" -> TypeName.INT
        "long" -> TypeName.LONG
        "float" -> TypeName.FLOAT
        "double" -> TypeName.DOUBLE
        else ->  ClassName.bestGuess(name).box()
    }
}

fun createKvFunction(entry: MappingEntry): MethodSpec {
    val main = MethodSpec.methodBuilder(generateKvFunctionName(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(getTypeName(entry.type) , entry.name)
            .returns(saClass)
            .addStatement("return keyValue(\$S,\$L)", entry.name, entry.name)
            .build()

    return main
}

private fun generateKvFunctionName(variableName: String) = "kv" + camelCase(variableName)

fun createListFunction(entry: MappingEntry): MethodSpec {
    val iterableType = ParameterizedTypeName.get(ClassName.get(Iterable::class.java), getTypeName(entry.type).box())

    return MethodSpec
            .methodBuilder("a" + camelCase(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(iterableType, entry.name)
            .returns(saClass)
            .addStatement("return new net.logstash.logback.marker.ObjectAppendingMarker(\$S,\$L)", entry.name, entry.name)
            .build()
}

fun createVarargFunction(entry: MappingEntry): MethodSpec {
    return MethodSpec.methodBuilder("a" + camelCase(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ArrayTypeName.of(getTypeName(entry.type)), entry.name).varargs()
            .returns(saClass)
            .addStatement("return new net.logstash.logback.marker.ObjectAppendingMarker(\$S,\$L)", entry.name, entry.name)
            .build()
}

fun createSentencesByCode(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")

    val sb1 : StringBuilder = StringBuilder("logger." + entry.defaultLevel + "(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name == v }.first()
        builder.addParameter(getTypeName(mapped.type), mapped.name)
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry)).append(")")

    builder.addStatement(sb1.toString())

    return builder.build()
}

fun createSentencesByCodeJDK7(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")
    builder.addParameter(getTypeName("org.slf4j.event.Level"), "level")

    val sb1 : StringBuilder = StringBuilder("(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name.equals(v) }.first()
        builder.addParameter(getTypeName(mapped.type), mapped.name)
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry)).append(")")


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
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    when(entry.variables.size) {
        0 ->     builder.addParameter(getTypeName("MonoConsumer"), "logger")
        1 ->     builder.addParameter(getTypeName("BiConsumer"), "logger")
        2 ->     builder.addParameter(getTypeName("TriConsumer"), "logger")
        else ->     builder.addParameter(getTypeName("ManyConsumer"), "logger")
    }

    val sb1 : StringBuilder = StringBuilder("logger.accept(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name.equals(v) }.first()
        builder.addParameter(getTypeName(mapped.type), mapped.name)
        sb1.append(" {}")
    }

    sb1.append("\"").append(createFunctionString(entry)).append(")")

    builder.addStatement(sb1.toString())

    return builder.build();
}



fun createSentencesByCodeWithLevel(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(getTypeName("org.slf4j.Logger"), "logger")
    val paramsArray : Array<Object> = Array(entry.variables.size) { i -> entry.variables[i] as Object}
    val sb1 : StringBuilder = StringBuilder("logger.info(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name.equals(v) }.first()
        builder.addParameter(getTypeName(mapped.type), mapped.name)
        sb1.append(" {}")
    }

    sb1.append("\"").append(createDollarString(entry)).append(")")

    builder.addStatement(sb1.toString(), *paramsArray)

    return builder.build();
}




fun createDollarString(entry: SentenceEntry): String {
    return if(entry.variables.isEmpty()) {
        ""
    } else {
        entry.variables.joinToString(",", prefix = ",")
    }

}

fun createFunctionString(entry: SentenceEntry): String {
    return if(entry.variables.isEmpty()) {
        ""
    } else {
        entry.variables.map { s -> generateKvFunctionName(s) + "(" + s + ")" }.joinToString(",", prefix = ",")
    }

}

fun createSentencesBySentence(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    return MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .build()
}