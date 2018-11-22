package com.navid.loggergenerator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.xenomachina.argparser.ArgParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

data class SayingEntry(val code: String, val message: String, val variables: List<String>, val extradata: Map<String, String>)
data class MappingEntry(val name: String, val type: String, val description: String,  val of: List<String>? )
data class MappingConfig(val version: Int, val mappings: List<MappingEntry>, val sayings: List<SayingEntry>)

fun main(args: Array<String>) {

    ArgParser(args).parseInto(::Arguments).run {
        val f = File(mappingFile)
        val mappingConfig = loadFromFile(f.toPath())

        generateJavaFile(mappingConfig, packageName, outputFolder)
    }
}


fun loadFromFile(path: Path): MappingConfig {
    val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
    mapper.registerModule(KotlinModule()) // Enable Kotlin support

    return Files.newBufferedReader(path).use {
        mapper.readValue(it, MappingConfig::class.java)
    }
}