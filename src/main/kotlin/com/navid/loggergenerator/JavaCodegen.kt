package com.navid.loggergenerator

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

val saClassName = ClassName.get("net.logstash.logback.argument", "StructuredArguments")
val saClass = ClassName.get("net.logstash.logback.argument", "StructuredArgument")

fun generateJavaFile(mappingConfig: MappingConfig, packageName: String) {
    val genClass = TypeSpec
            .classBuilder("LoggerUtils")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

    mappingConfig.mappings.forEach{
        genClass.addMethod(createKvFunction(it))
        genClass.addMethod(createListFunction(it))
        genClass.addMethod(createVarargFunction(it))
    }


    val javaFile = JavaFile
            .builder(packageName, genClass.build())
            .addStaticImport(saClassName, "*")
            .build()

    javaFile.writeTo(System.out)
}

fun camelCase(string: String): String {
    return string.substring(0,1).toUpperCase() + string.substring(1)
}

fun createKvFunction(entry: MappingEntry): MethodSpec {
    val main = MethodSpec.methodBuilder("kv" + camelCase(entry.name))
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
            .addParameter(iterableType, entry.name)
            .returns(saClass)
            .addStatement("return array(\$S,\$L)", entry.name, entry.name)
            .build()
}

fun createVarargFunction(entry: MappingEntry): MethodSpec {
    return MethodSpec.methodBuilder("a" + camelCase(entry.name))
            .addParameter(ArrayTypeName.of(ClassName.bestGuess(entry.type).box()), entry.name).varargs()
            .returns(saClass)
            .addStatement("return array(\$S,\$L)", entry.name, entry.name)
            .build()
}