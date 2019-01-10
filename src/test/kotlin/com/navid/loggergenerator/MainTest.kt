package com.navid.loggergenerator

import org.junit.Test as test

class TestSource() {
    @test fun testJdk7() {
        //given
        var filePath = TestSource::class.java.getResource("/mapping.yml")

        //when
        com.navid.loggergenerator.main(
                arrayOf(
                        "--input", filePath.path,
                        "--package", "com.example.helloworld",
                        "--codegen-output", "build/test/codegen7",
                        "--html-output", "build/test/html7",
                        "--class-name", "LoggerUtils",
                        "--html-name", "LoggerExported.html",
                        "--compat-1.7"
                ))

        //then
    }

    @test fun testJdk8() {
        //given
        var filePath = com.navid.loggergenerator.TestSource::class.java.getResource("/mapping.yml")

        //when
        com.navid.loggergenerator.main(
                arrayOf(
                        "--input", filePath.path,
                        "--package", "com.example.helloworld",
                        "--codegen-output", "build/test/codegen8",
                        "--html-output", "build/test/html8",
                        "--class-name", "LoggerUtils",
                        "--html-name", "LoggerExported.html",
                        "--compat-1.8"
                ))

        //then
    }
}