package com.navid.loggergenerator.config

class MappingEntry {
    private var name: String? = null

    private var type: String? = null

    private var description: String? = null

    fun getName(): String? {
        return name
    }

    fun setName(name: String): MappingEntry {
        this.name = name
        return this
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String): MappingEntry {
        this.type = type
        return this
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String): MappingEntry {
        this.description = description
        return this
    }
}