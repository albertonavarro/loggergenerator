package com.navid.loggergenerator.config

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.ArrayList

class MappingConfig {

    private var version: Int? = 1

    @JsonProperty("project-name")
    private var projectName: String? = null

    private var mappings: List<MappingEntry> = ArrayList()

    private var sentences: List<SentenceEntry> = ArrayList()

    private var context: List<String> = ArrayList()

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

    fun getContext(): List<String> {
        return context
    }

    fun setContext(context: List<String>?): MappingConfig {
        this.context = context ?: ArrayList()
        return this
    }

    fun getProjectName(): String? {
        return projectName
    }

    fun setProjectName(projectName: String): MappingConfig {
        this.projectName = projectName
        return this
    }

}