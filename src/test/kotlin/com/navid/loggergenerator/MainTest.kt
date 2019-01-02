package com.navid.loggergenerator

import org.junit.Test as test

class TestSource() {
    @test fun testMain() {
        //given
        var filePath = TestSource::class.java.getResource("/mapping.yml")

        //when
        com.navid.loggergenerator.main(
                arrayOf(
                        "--input", filePath.path,
                        "--package", "com.example.helloworld",
                        "--codegen-output", "zzz",
                        "--html-output", "yyy",
                        "--class-name", "LU",
                        "--html-name", "T1.html"))

        //then
    }
}