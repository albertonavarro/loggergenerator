package com.navid.loggergenerator.config

import org.junit.Test as test
import org.junit.rules.ExpectedException
import org.junit.Rule



class TestSource {

    @Rule @JvmField
     var exception = ExpectedException.none()

    @test fun testValidationNoVersion() {
        //given
        val input: MappingConfig = MappingConfig().setVersion(2)
        exception.expect(ValidationException::class.java)
        exception.expectMessage("version missing or wrong, it must be 1")

        //when
        ConfigValidation(input).validate()
    }
}