package com.navid.loggergenerator.config

import java.lang.Exception

class ValidationException(errorMessages: ArrayList<String>) : Exception(errorMessages.joinToString(separator = "\n")) {

}
