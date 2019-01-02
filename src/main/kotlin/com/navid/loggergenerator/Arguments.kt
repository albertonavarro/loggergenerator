package com.navid.loggergenerator

import com.xenomachina.argparser.ArgParser

class Arguments(parser: ArgParser) {
    val mappingFile by parser.storing("-I", "--input",
            help = "Mapping file")
    val packageName by parser.storing("-P", "--package",
            help = "Codegen generated package")
    val outputFolder by parser.storing("-C", "--codegen-output",
            help = "Codegen Output folder")
    val javaClassName by parser.storing("-J", "--class-name",
            help = "Java class name")
    val htmlName by parser.storing("-N", "--html-name",
            help = "Html file name")
    val htmlOutputFolder by parser.storing("-H", "--html-output",
            help = "Html Output folder")
}