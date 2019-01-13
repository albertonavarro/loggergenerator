package com.navid.loggergenerator

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

enum class SentenceNamingStrategy {
    BY_CODE, BY_SENTENCE
}

enum class JavaCompatibility {
    JAVA7, JAVA8
}

class Arguments(parser: ArgParser) {
    val mappingFile by parser.storing("-I", "--input",
            help = "Mapping file")
    val packageName by parser.storing("-P", "--package-name",
            help = "Codegen generated package").default("com.example")
    val codegenOutput by parser.storing("-C", "--codegen-output",
            help = "Codegen Output folder").default(".")
    val javaClassName by parser.storing("-J", "--class-name",
            help = "Java class name").default("LoggerUtils")
    val htmlName by parser.storing("-N", "--html-name",
            help = "Html file name").default("LoggerUtilsDoc.html")
    val htmlOutputFolder by parser.storing("-H", "--html-output",
            help = "Html Output folder").default(".")
    val sentenceNamingStrategy by parser.mapping(
            "--by-code" to SentenceNamingStrategy.BY_CODE,
            "--by-sentence" to SentenceNamingStrategy.BY_SENTENCE,
            help = "Sentence naming strategy").default(SentenceNamingStrategy.BY_CODE)
    val javaCompatibility by parser.mapping(
            "--compat-1.7" to JavaCompatibility.JAVA7,
            "--compat-1.8" to JavaCompatibility.JAVA8,
            help = "Java compatibility mode").default(JavaCompatibility.JAVA8)

}