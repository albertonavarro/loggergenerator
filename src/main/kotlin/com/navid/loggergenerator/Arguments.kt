package com.navid.loggergenerator

import com.xenomachina.argparser.ArgParser

class Arguments(parser: ArgParser) {
    val mappingFile by parser.storing("-I", "--input",
            help = "Mapping file")
    val packageName by parser.storing("-P", "--package",
            help = "Codegen generated package")
    val outputFolder by parser.storing("-O", "--output",
            help = "Output folder")

}