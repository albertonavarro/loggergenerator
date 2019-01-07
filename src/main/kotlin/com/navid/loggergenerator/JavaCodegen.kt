package com.navid.loggergenerator

import com.squareup.javapoet.*
import java.io.File
import java.io.ObjectInput
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

    validateMappingConfig(mappingConfig)

    val sentenceCreationMethod : NamingStrategy =
            if(namingStrategy == SentenceNamingStrategy.BY_CODE )
                {s, c -> createSentencesByCode(s, c)}
            else
                {s, c -> createSentencesBySentence(s, c) }

    if (javaCompat.ordinal > JavaCompatibility.JAVA7.ordinal) {
        genClass.addType(TypeSpec.interfaceBuilder("MonoConsumer")
                .addMethod(MethodSpec.methodBuilder("accept")
                        .addParameter( ClassName.bestGuess("String").box(), "var1")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .build())

        genClass.addType(TypeSpec.interfaceBuilder("BiConsumer")
                .addMethod(MethodSpec.methodBuilder("accept")
                        .addParameter( ClassName.bestGuess("String").box(), "var1")
                        .addParameter( ClassName.bestGuess("Object").box(), "var2")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .build())

        genClass.addType(TypeSpec.interfaceBuilder("TriConsumer")
                .addMethod(MethodSpec.methodBuilder("accept")
                        .addParameter( ClassName.bestGuess("String").box(), "var1")
                        .addParameter( ClassName.bestGuess("Object").box(), "var2")
                        .addParameter( ClassName.bestGuess("Object").box(), "var3")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .build())

        genClass.addType(TypeSpec.interfaceBuilder("ManyConsumer")
                .addMethod(MethodSpec.methodBuilder("accept")
                        .addParameter( ClassName.bestGuess("String").box(), "var1")
                        .addParameter(ArrayTypeName.of(ClassName.bestGuess("Object").box()), "var2").varargs()
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .build())
    }


    mappingConfig.mappings.forEach{
        genClass.addMethod(createKvFunction(it))
        genClass.addMethod(createListFunction(it))
        genClass.addMethod(createVarargFunction(it))
    }

    mappingConfig.sentences.forEach {
        genClass.addMethod(sentenceCreationMethod(it, mappingConfig))
    }

    val javaFile = JavaFile
            .builder(packageName, genClass.build())
            .addStaticImport(saClassName, "*")
            .build()


    javaFile.writeTo(File(outputFolder))
}

fun validateMappingConfig(mappingConfig: MappingConfig) {
//logger is a reserved word
}

fun camelCase(string: String): String {
    return string.substring(0,1).toUpperCase() + string.substring(1)
}

fun createKvFunction(entry: MappingEntry): MethodSpec {
    val main = MethodSpec.methodBuilder("kv" + camelCase(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter( ClassName.bestGuess(entry.type).box(), entry.name)
            .returns(saClass)
            .addStatement("return keyValue(\$S,\$L)", entry.name, entry.name)
            .build()

    return main
}

fun createListFunction(entry: MappingEntry): MethodSpec {
    val iterableType = ParameterizedTypeName.get(ClassName.get(Iterable::class.java), ClassName.bestGuess(entry.type).box())

    return MethodSpec
            .methodBuilder("a" + camelCase(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(iterableType, entry.name)
            .returns(saClass)
            .addStatement("return array(\$S,\$L)", entry.name, entry.name)
            .build()
}

fun createVarargFunction(entry: MappingEntry): MethodSpec {
    return MethodSpec.methodBuilder("a" + camelCase(entry.name))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ArrayTypeName.of(ClassName.bestGuess(entry.type).box()), entry.name).varargs()
            .returns(saClass)
            .addStatement("return array(\$S,\$L)", entry.name, entry.name)
            .build()
}

fun createSentencesByCode(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(ClassName.bestGuess("org.slf4j.Logger").box(), "logger")
    val paramsArray : Array<Object> = Array(entry.variables.size) { i -> entry.variables[i] as Object}
    val sb1 : StringBuilder = StringBuilder("logger.info(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name.equals(v) }.first()
        builder.addParameter(ClassName.bestGuess(mapped.type).box(), mapped.name)
        sb1.append(" {}")
    }

    sb1.append("\"").append(createDollarString(entry)).append(")")

    builder.addStatement(sb1.toString(), *paramsArray)

    return builder.build();
}



fun createSentencesByCodeWithLevel(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    val builder = MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    builder.addParameter(ClassName.bestGuess("org.slf4j.Logger").box(), "logger")
    val paramsArray : Array<Object> = Array(entry.variables.size) { i -> entry.variables[i] as Object}
    val sb1 : StringBuilder = StringBuilder("logger.info(\"").append(entry.message)

    entry.variables.forEach{v ->
        val mapped = config.mappings.filter { m -> m.name.equals(v) }.first()
        builder.addParameter(ClassName.bestGuess(mapped.type).box(), mapped.name)
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

fun createSentencesBySentence(entry: SentenceEntry, config: MappingConfig): MethodSpec {
    return MethodSpec.methodBuilder("audit" + camelCase(entry.code))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .build()
}