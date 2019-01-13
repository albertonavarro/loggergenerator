package com.navid.loggergenerator.config

import java.util.ArrayList

class MappingConfig {

    private var version: Int? = 1

    private var mappings: List<MappingEntry> = ArrayList()

    private var sentences: List<SentenceEntry> = ArrayList()

    fun getVersion(): Int? {
        return version
    }

    fun setVersion(version: Int?): MappingConfig {
        this.version = version
        return this
    }

    fun getMappings(): List<MappingEntry> {
        return mappings
    }

    fun setMappings(mappings: List<MappingEntry>?): MappingConfig {
        this.mappings = mappings ?: ArrayList()
        return this
    }

    fun getSentences(): List<SentenceEntry> {
        return sentences
    }

    fun setSentences(sentences: List<SentenceEntry>?): MappingConfig {
        this.sentences = sentences ?: ArrayList()
        return this
    }
}