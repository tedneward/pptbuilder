package com.newardassociates.pptbuilder

import org.xml.sax.InputSource
import org.w3c.dom.Document as XMLDocument
import org.w3c.dom.Node as XMLNode
import org.w3c.dom.NodeList as XMLNodeList
import java.io.File
import java.io.StringReader
import java.util.logging.Logger
import javax.xml.parsers.*
import javax.xml.xpath.*
import java.util.logging.*

class Parser {
    private val logger : Logger = Logger.getLogger(Parser::class.java.canonicalName)

    fun parse(file : File) : Presentation {
        logger.info("Parsing ${file.absoluteFile}")
        return parse(factory.newDocumentBuilder().parse(file))
    }
    fun parse(contents : String) : Presentation {
        logger.info("Parsing string contents")
        return parse(factory.newDocumentBuilder().parse(InputSource(StringReader(contents))))
    }

    /* XML Machinery */
    private val factory = DocumentBuilderFactory.newInstance()
    init {
        factory.isNamespaceAware = true
        factory.isXIncludeAware = true
    }
    private val xpath: XPath = XPathFactory.newInstance().newXPath()
    private val titleXPath : XPathExpression = xpath.compile("/presentation/head/title/text()")
    private val abstractXPath : XPathExpression= xpath.compile("/presentation/head/abstract/text()")
    private val audienceXPath : XPathExpression= xpath.compile("/presentation/head/audience/text()")
    private fun parse(doc : XMLDocument): Presentation {
        check(doc.documentElement.tagName == "presentation")

        val title = titleXPath.evaluate(doc, XPathConstants.STRING).toString()
        val abstract = abstractXPath.evaluate(doc, XPathConstants.STRING).toString()
        val audience = audienceXPath.evaluate(doc, XPathConstants.STRING).toString()
        return Presentation(title, abstract, audience, parseNodes(doc.documentElement.childNodes))
    }
    private fun parseNodes(nodes : XMLNodeList): List<Node> {
        val ast = mutableListOf<Node>()

        var sectionTitle : String = ""

        for (i in 0 until nodes.length) {
            val node = nodes.item(i)

            when (node.nodeName) {
                // At some point, make "title" optional and default to current "section" title
                "slide" -> {
                    val titleNode = node.attributes.getNamedItem("title")
                    val title = if (titleNode != null) titleNode.nodeValue else sectionTitle
                    val rawBody = node.textContent
                    logger.info("Parsing slide title=${title}")
                    ast.add(Slide(title, rawBody))
                }
                "section" -> {
                    sectionTitle = node.attributes.getNamedItem("title").nodeValue
                    // Get subtitle, quote, and attribution, if present; either subtitle or quote
                    // has to be there, and attribution should only be there if quote is
                    logger.info("Parsing section title=${sectionTitle}")
                    ast.add(Section(sectionTitle, null, null))
                }
                "slideset" -> {
                    logger.info("Parsing slideset")
                    ast.addAll(parseNodes(node.childNodes))
                    logger.info("End slideset")
                }
                // "head" gets ignored
                else -> print("Ignoring ${node.nodeName}")
            }
        }
        return ast
    }
}
