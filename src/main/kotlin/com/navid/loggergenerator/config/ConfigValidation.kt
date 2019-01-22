package com.navid.loggergenerator.config

import java.util.regex.Pattern

const val nameRegex = "^[a-zA-Z_\$][a-zA-Z_\$0-9]*\$"
val namePattern = Pattern.compile(nameRegex)

data class ConfigValidation(val config: MappingConfig) {

    fun validate() {
        val errorMessages : ArrayList<String> = ArrayList()
        errorMessages += validateVersion()
        errorMessages += validateProjectName()
        errorMessages += validateCompleteMappings()
        errorMessages += validateCompleteSentences()
        errorMessages += validateMappingSentenceTypeReferences()
        errorMessages += validateMappingContextTypeReferences()

        if(!errorMessages.isEmpty()) {
            throw ValidationException(errorMessages)
        }
    }

    private fun validateProjectName(): List<String> {
        val result : ArrayList<String> = ArrayList()
        if (config.getProjectName() == null) {
            result.add("project-name == null. Project name can't be null.")
        } else {
            if (config.getProjectName()!!.isEmpty()) {
                result.add("project-name.size == 0. Project name can't be empty")
            }
        }
        return result
    }

    private fun validateMappingContextTypeReferences(): List<String> {
        val result : ArrayList<String> = ArrayList()

        return result
    }

    private fun validateVersion(): List<String> {
        val result : ArrayList<String> = ArrayList()
        if( config.getVersion() == null || config.getVersion() != 1) {
            result.add("version missing or wrong, it must be 1")
        }
        return result
    }

    private fun validateMappingSentenceTypeReferences(): List<String> {
        val result : ArrayList<String> = ArrayList()

        return result
    }

    private fun validateCompleteSentences(): List<String> {
        val result : ArrayList<String> = ArrayList()

        for (index in (config.getSentences() as List<*>).indices) {
            val entry = config.getSentences()[index]

            if (entry.getCode() == null) {
                result.add("sentences[$index].code == code. Sentence code entry can't be null.")
            } else {
                if (!namePattern.matcher(entry.getCode()).matches()) {
                    result.add("sentences[$index].code regex error. Sentence code doesn't follow variable syntax $nameRegex")
                }
            }

            if (entry.getMessage() == null) {
                result.add("sentences[$index].message == null. Sentence message entry can't be null.")
            } else {
                if (entry.getMessage()!!.isEmpty()) {
                    result.add("sentences[$index].message.size == 0. Sentence message can't be empty")
                }
            }

            if (entry.getDefaultLevel() == null) {
                result.add("sentences[$index].defaultLevel == null. Mapping defaultLevel entry can't be null.")
            } else {
                if (entry.getDefaultLevel()!!.isEmpty()) {
                    result.add("sentences[$index].defaultLevel.size == 0. Mapping defaultLevel can't be empty")
                }
            }
        }

        return result
    }

    fun validateCompleteMappings() : List<String> {
        val result : ArrayList<String> = ArrayList()

        for(index in config.getMappings().indices) {

            val entry = (config.getMappings()[index])

            if (entry.getName() == null) {
                result.add("mappings[$index].name == null. Mapping name entry can't be null.")
            } else {
                if (!namePattern.matcher(entry.getName()).matches()) {
                    result.add("mappings[$index].name regex error. Mapping name doesn't follow variable syntax $nameRegex")
                }
            }

            if (entry.getType() == null) {
                result.add("mappings[$index].type == null. Mapping type entry can't be null.")
            } else {
                if (entry.getType()!!.isEmpty()) {
                    result.add("mappings[$index].type.size == 0. Mapping type can't be empty")
                }
            }

            if (entry.getDescription() == null) {
                result.add("mappings[$index].description == null. Mapping description entry can't be null.")
            } else {
                if (entry.getDescription()!!.isEmpty()) {
                    result.add("mappings[$index].description.size == 0. Mapping description can't be empty")
                }
            }
        }


        return result
    }

}