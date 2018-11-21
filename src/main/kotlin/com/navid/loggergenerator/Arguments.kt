package com.navid.loggergenerator

import com.xenomachina.argparser.ArgParser

class Arguments(parser: ArgParser) {
    val mappingFile by parser.storing("-I", "--input",
            help = "mapping file")
    val packageName by parser.storing("-P", "--package",
            help = "codegen generated package")

}