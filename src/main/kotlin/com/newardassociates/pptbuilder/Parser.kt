package com.newardassociates.pptbuilder

import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File
import java.io.StringReader
import java.util.logging.Logger
import javax.xml.parsers.*
import javax.xml.xpath.*
import org.xml.sax.InputSource
import java.util.*
import org.w3c.dom.Document as XMLDocument
import org.w3c.dom.Node as XMLNode
import org.w3c.dom.NodeList as XMLNodeList
import com.vladsch.flexmark.parser.Parser as MDParser
import com.vladsch.flexmark.util.ast.Node as MDNode

class Parser(val properties : Properties) {
    private val logger = Logger.getLogger(Parser::class.java.canonicalName)

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

    // Let's be honest, the use of XPath here is a little gratuitous, since these are
    // all fairly closely-clustered together and could've just as easily been navigated
    // by hand. But this way is a little bit clearer, and I argue more maintainable, even
    // if XPath is historically not highly-performant.
    private val xpath: XPath = XPathFactory.newInstance().newXPath()
    private val titleXPath = xpath.compile("/presentation/head/title/text()")
    private val abstractXPath = xpath.compile("/presentation/head/abstract/text()")
    private val audienceXPath = xpath.compile("/presentation/head/audience/text()")
    private val authorXPath = xpath.compile("/presentation/head/author/name/text()")
    private val affiliationXPath = xpath.compile("/presentation/head/author/affiliation/text()")
    private val contactXPath = xpath.compile("/presentation/head/author/contact")
    private val categoryXPath = xpath.compile("/presentation/head/category")

    private fun parse(doc : XMLDocument): Presentation {
        check(doc.documentElement.tagName == "presentation")
        logger.info("Parsing Document instance w/root element ${doc.documentElement.tagName}")

        val title = titleXPath.evaluate(doc, XPathConstants.STRING).toString()
        val author = authorXPath.evaluate(doc, XPathConstants.STRING).toString()
        val affiliation = affiliationXPath.evaluate(doc, XPathConstants.STRING).toString()
        val abstract = abstractXPath.evaluate(doc, XPathConstants.STRING).toString()
        val audience = audienceXPath.evaluate(doc, XPathConstants.STRING).toString()

        val contacts = contactXPath.evaluate(doc, XPathConstants.NODESET) as XMLNodeList
        val contactInfo = mutableMapOf<String, String>()
        // plop in contact info defaults first
        if (properties.getProperty("contact.email") != null) contactInfo["email"] = properties.getProperty("contact.email")
        if (properties.getProperty("contact.twitter") != null) contactInfo["twitter"] = properties.getProperty("contact.twitter")
        if (properties.getProperty("contact.linkedin") != null) contactInfo["linkedin"] = properties.getProperty("contact.linkedin")
        if (properties.getProperty("contact.blog") != null) contactInfo["blog"] = properties.getProperty("contact.blog")
        logger.info("Propertes after defaults: ${properties}")

        if (contacts.length > 0) {
            for (n in 0..contacts.length-1) {
                val node = contacts.item(n)
                logger.info("Parsing ${node}")
                when (node.localName) {
                    "email" -> contactInfo["email"] = node.textContent
                    "blog" -> contactInfo["blog"] = node.textContent
                    "twitter" -> contactInfo["twitter"] = node.textContent
                    "linkedin" -> contactInfo["linkedin"] = node.textContent
                }
            }
        }

        val categories = categoryXPath.evaluate(doc, XPathConstants.NODESET) as XMLNodeList
        val keywords = mutableListOf<String>()
        if (categories.length > 0) {
            // pull out the categories for keywords in the doc
            for (n in 0..categories.length-1) {
                val node = categories.item(n)
                keywords.add(node.textContent)
            }
        }

        return Presentation(title, abstract, audience,
                if (author == "") properties.get("author").toString() else author,
                if (affiliation == "") properties.get("affiliation").toString() else affiliation,
                contactInfo, keywords,
                parseNodes(doc.documentElement.childNodes))
    }
    private fun parseNodes(nodes : XMLNodeList): List<Node> {
        val ast = mutableListOf<Node>()
        val mdParserOptions = MutableDataSet()
        val mdParser = MDParser.builder(mdParserOptions).build()

        var sectionTitle = ""

        for (i in 0 until nodes.length) {
            val node = nodes.item(i)

            when (node.nodeName) {
                // At some point, make "title" optional and default to current "section" title
                "slide" -> {
                    val titleNode = node.attributes.getNamedItem("title")
                    val title = if (titleNode != null) titleNode.nodeValue else sectionTitle
                    logger.info("Parsing slide title=${title}")
                    ast.add(Slide(title, mdParser.parse(node.textContent.trimIndent()), node.textContent))
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
                //else -> print("Ignoring ${node.nodeName}")
            }
        }
        return ast
    }

    private fun bodyTrim(incoming : String) : String {
        var raw = incoming
        logger.info("Trimming ||${raw}||")

        // Count how many whitespace chars the Markdown appears to be
        // indented by; we need to trim that many whitespace chars in
        // front of each line. Complication: the first line might be all
        // whitespace, thanks to the wonderful ways XML parsers treat
        // text nodes.
        var count = 0
        while (raw[count].isWhitespace()) {
            when (raw[count]) {
                '\n' -> {
                    raw = raw.substring(count + 1)
                    count = 0
                }
                ' ', '\t' -> count += 1
            }
        }
        logger.info("There appear to be $count whitespace characters in front of each line")

        var trimmed = ""
        for (line in raw.trimEnd().split('\n')) {
            if (line.trim().isEmpty())
                continue
            trimmed += line.substring(count) + "\n"
        }

        logger.info("Returning trimmed string to look like ||${trimmed}||")
        return trimmed
    }
}
