package com.navid.loggergenerator.config

import java.util.ArrayList
import java.util.HashMap

class SentenceEntry {
    private var code: String? = null

    private var message: String? = null

    private var defaultLevel: String? = null

    private var variables: List<String> = ArrayList()

    private var extradata: Map<String, String> = HashMap()

    fun getCode(): String? {
        return code
    }

    fun setCode(code: String): SentenceEntry {
        this.code = code
        return this
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String): SentenceEntry {
        this.message = message
        return this
    }

    fun getDefaultLevel(): String? {
        return defaultLevel
    }

    fun setDefaultLevel(defaultLevel: String): SentenceEntry {
        this.defaultLevel = defaultLevel
        return this
    }

    fun getVariables(): List<String> {
        return variables
    }

    fun setVariables(variables: List<String>?): SentenceEntry {
        this.variables = variables ?: ArrayList()
        return this
    }

    fun getExtradata(): Map<String, String> {
        return extradata
    }

    fun setExtradata(extradata: Map<String, String>?): SentenceEntry {
        this.extradata = extradata ?: HashMap()
        return this
    }
}