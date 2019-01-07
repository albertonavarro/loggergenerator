package com.navid.loggergenerator

import java.io.File

fun genHtml(mappingConfig: MappingConfig, htmlFileName: String, outputFolder: String) {
    val result =
            html {
                head {
                    title { +"Project Log Reference" }
                }
                body {
                    h1 { +"Project Log Reference" }

                    h2 { +"What's this document" }
                    p { +"This document summarizes all exportable log knowledge for this project." }

                    h2 { +"Declared types:" }
                    table {
                        thead{
                            tr{
                                td{ +"Attribute name" }
                                td{ +"Attribute type"}
                                td{ +"Description"}
                            }
                        }
                        tbody{
                            for (me in mappingConfig.mappings) {
                                tr{
                                    td{ +me.name }
                                    td{ +me.type}
                                    td{ +me.description}
                                }
                            }
                        }
                    }

                    h2 { +"Auditable sentences:" }
                    table {
                        thead{
                            tr{
                                td{ +"Code" }
                                td{ +"Message"}
                                td{ +"Variables"}
                                td{ +"Extra data"}
                            }
                        }
                        tbody{
                            for (saying in mappingConfig.sentences) {
                                tr{
                                    td{ + saying.code }
                                    td{ + saying.message}
                                    td{
                                        ul {
                                            for (variable in saying.variables) {
                                                li { +variable}
                                            }
                                        }

                                    }
                                    td {
                                        ul {
                                            for (extradata in saying.extradata!!) {
                                                li { +(extradata.key + "=" + extradata.value)}
                                            }
                                        }
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
}

class Title() : TagWithText("title")

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
    fun td(init: TD.() -> Unit) = initTag(TD(), init)
}

class TD(): BodyTag("td")

class A() : BodyTag("a") {
    public var href: String
        get() = attributes["href"]!!
        set(value) {
            attributes["href"] = value
        }
}

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}