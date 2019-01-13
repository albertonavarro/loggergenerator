package com.navid.loggergenerator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.navid.loggergenerator.config.ConfigValidation
import com.navid.loggergenerator.config.MappingConfig
import com.xenomachina.argparser.ArgParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {

    ArgParser(args).parseInto(::Arguments).run {
        val f = File(mappingFile)
        val mappingConfig = loadFromFile(f.toPath())
        ConfigValidation(mappingConfig).validate()


        genHtml( mappingConfig, htmlName, htmlOutputFolder)
        generateJavaFile(mappingConfig, packageName, javaClassName, codegenOutput, sentenceNamingStrategy, javaCompatibility)
    }
}


fun loadFromFile(path: Path): MappingConfig {
    val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
    mapper.registerModule(KotlinModule()) // Enable Kotlin support

    return Files.newBufferedReader(path).use {
        mapper.readValue(it, MappingConfig::class.java)
    }
}

