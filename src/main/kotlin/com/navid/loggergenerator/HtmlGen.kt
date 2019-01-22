package com.navid.loggergenerator

import com.navid.loggergenerator.config.MappingConfig
import java.io.File

fun genHtml(mappingConfig: MappingConfig, htmlFileName: String, outputFolder: String) {
    val result =
            html {
                head {
                    title { + (mappingConfig.getProjectName() + " Log Reference") }
                    style {+("table {\n" +
                            "  border: 1px solid #1C6EA4;\n" +
                            "  background-color: #EEEEEE;\n" +
                          //  "  width: 100%;\n" +
                            "  text-align: left;\n" +
                            "  border-collapse: collapse;\n" +
                            "}\n" +
                            "table td, table th {\n" +
                            "  border: 1px solid #AAAAAA;\n" +
                            "  padding: 3px 2px;\n" +
                            "}\n" +
                            "table tr:target {\n" +
                            "  background-color: #ffa;\n" +
                            "}\n" +
                            "table tbody td {\n" +
                            "  font-size: 13px;\n" +
                            "}\n" +
                            "table thead {\n" +
                            "  background: #1C6EA4;\n" +
                            "  background: -moz-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\n" +
                            "  background: -webkit-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\n" +
                            "  background: linear-gradient(to bottom, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\n" +
                            "  border-bottom: 2px solid #444444;\n" +
                            "}\n" +
                            "table thead th {\n" +
                            "  font-size: 15px;\n" +
                            "  font-weight: bold;\n" +
                            "  color: #FFFFFF;\n" +
                            "  border-left: 2px solid #D0E4F5;\n" +
                            "}\n" +
                            "table thead th:first-child {\n" +
                            "  border-left: none;\n" +
                            "}\n" +
                            "\n" +
                            "table tfoot {\n" +
                            "  font-size: 14px;\n" +
                            "  font-weight: bold;\n" +
                            "  color: #FFFFFF;\n" +
                            "  background: #D0E4F5;\n" +
                            "  background: -moz-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n" +
                            "  background: -webkit-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n" +
                            "  background: linear-gradient(to bottom, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n" +
                            "  border-top: 2px solid #444444;\n" +
                            "}\n" +
                            "table tfoot td {\n" +
                            "  font-size: 14px;\n" +
                            "}\n" +
                            "table tfoot .links {\n" +
                            "  text-align: right;\n" +
                            "}\n" +
                            "table tfoot .links a{\n" +
                            "  display: inline-block;\n" +
                            "  background: #1C6EA4;\n" +
                            "  color: #FFFFFF;\n" +
                            "  padding: 2px 8px;\n" +
                            "  border-radius: 5px;\n" +
                            "}" )}
                }
                body {
                    h1 { + (mappingConfig.getProjectName() + " Log Reference") }

                    h2 { +"What's this document" }
                    p { +"This document summarizes all exportable log knowledge for this project." }
                    p { +"Attribute names might differ from final ones depending on your Logback configuration." }

                    h2 { +"Declared types:" }
                    table {
                        thead{
                            tr{
                                th{ +"Attribute name" }
                                th{ +"Attribute type"}
                                th{ +"Description"}
                            }
                        }
                        tbody{
                            for (me in mappingConfig.getMappings()) {
                                trId(me.getName()!!) {
                                    td {
                                        +me.getName()!!
                                    }
                                    td { +me.getType()!! }
                                    td { +me.getDescription()!! }
                                }
                            }
                        }
                    }

                    h2 { +"Auditable sentences:" }
                    table {
                        thead{
                            tr{
                                th{ +"Code" }
                                th{ +"Message"}
                                th{ +"Variables"}
                                th{ +"Extra data"}
                            }
                        }
                        tbody{
                            for (saying in mappingConfig.getSentences()) {
                                tr{
                                    td{ + saying.getCode()!! }
                                    td{ + saying.getMessage()!!}
                                    td{
                                        ul {
                                            for (variable in saying.getVariables()) {
                                                li { a(href = "#$variable") { +"$variable" }}
                                            }
                                        }

                                    }
                                    td {
                                        ul {
                                            for (extradata in saying.getExtradata()!!) {
                                                li { +(extradata.key + "=" + extradata.value)}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    h2 { +"Contextual information" }
                    table {
                        thead{
                            tr{
                                th{ +"Attribute name" }
                                th{ +"Type reference" }
                            }
                        }
                        tbody{
                            for (contextKey in mappingConfig.getContext()) {
                                tr{
                                    td{ + ("ctx.$contextKey") }
                                    td {
                                        a(href = "#$contextKey") { +"$contextKey" }
                                    }
                                }

                            }
                        }
                    }
                }
            }

    val newFile = File(outputFolder, htmlFileName)
    newFile.parentFile.mkdirs()
    newFile.writeText(result.toString())
}

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}>\n")
        for (c in children) {
            c.render(builder, indent + "  ")
        }
        builder.append("$indent</$name>\n")
    }

    private fun renderAttributes(): String? {
        val builder = StringBuilder()
        for (a in attributes.keys) {
            builder.append(" $a=\"${attributes[a]}\"")
        }
        return builder.toString()
    }


    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class HTML() : TagWithText("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)

    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head() : TagWithText("head") {
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun style(init: Style.() -> Unit) = initTag(Style(), init)
}

class Title() : TagWithText("title")
class Style() : TagWithText("style")


abstract class BodyTag(name: String) : TagWithText(name) {
    fun b(init: B.() -> Unit) = initTag(B(), init)
    fun p(init: P.() -> Unit) = initTag(P(), init)
    fun h1(init: H1.() -> Unit) = initTag(H1(), init)
    fun h2(init: H2.() -> Unit) = initTag(H2(), init)
    fun ul(init: UL.() -> Unit) = initTag(UL(), init)
    fun table(init: Table.() -> Unit) = initTag(Table(), init)
    fun a(href: String, init: A.() -> Unit) {
        val a = initTag(A(), init)
        a.href = href
    }
    fun trId(id: String, init: TR.() -> Unit) {
        val tr = initTag(TR(), init)
        tr.id = id
    }
    fun aId(id: String, init: A.() -> Unit) {
        val a = initTag(A(), init)
        a.id = id
    }
}

class Body() : BodyTag("body")
class UL() : BodyTag("ul") {
    fun li(init: LI.() -> Unit) = initTag(LI(), init)
}

class B() : BodyTag("b")
class LI() : BodyTag("li")
class P() : BodyTag("p")
class H1() : BodyTag("h1")
class H2() : BodyTag("h2")
class Table(): BodyTag("table") {
    fun thead(init: THead.() -> Unit) = initTag(THead(), init)
    fun tbody(init: TBody.() -> Unit) = initTag(TBody(), init)
}
class THead(): BodyTag("thead") {
    fun tr(init: TR.() -> Unit) = initTag(TR(), init)
}
class TBody(): BodyTag("tbody") {
    fun tr(init: TR.() -> Unit) = initTag(TR(), init)
}
class TR(): BodyTag("tr") {
    var id: String
        get() = attributes["id"]!!
        set(value) {
            attributes["id"] = value
        }
    fun td(init: TD.() -> Unit) = initTag(TD(), init)
    fun th(init: TH.() -> Unit) = initTag(TH(), init)
}

class TH(): BodyTag("th") {
    fun th(init: TH.() -> Unit) = initTag(TH(), init)
}

class TD(): BodyTag("td")

class A() : BodyTag("a") {
    var href: String
        get() = attributes["href"]!!
        set(value) {
            attributes["href"] = value
        }
    var id: String
        get() = attributes["id"]!!
        set(value) {
            attributes["id"] = value
        }
}

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}