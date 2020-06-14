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
    private val jobTitleXPath = xpath.compile("/presentation/head/author/title/text()")
    private val contactXPath = xpath.compile("/presentation/head/author/contact/*")
    private val categoryXPath = xpath.compile("/presentation/head/category")
    private val slideNotesXPath = xpath.compile("./notes")

    private fun parse(doc : XMLDocument): Presentation {
        check(doc.documentElement.tagName == "presentation")
        logger.info("Parsing Document instance w/root element ${doc.documentElement.tagName}")

        // Talk-specific bits
        val title = titleXPath.evaluate(doc, XPathConstants.STRING).toString()
        val abstract = abstractXPath.evaluate(doc, XPathConstants.STRING).toString()
        val audience = audienceXPath.evaluate(doc, XPathConstants.STRING).toString()

        val categories = categoryXPath.evaluate(doc, XPathConstants.NODESET) as XMLNodeList
        val keywords = mutableListOf<String>()
        if (categories.length > 0) {
            // pull out the categories for keywords in the doc
            for (n in 0..categories.length-1) {
                val node = categories.item(n)
                keywords.add(node.textContent)
            }
        }

        // Author-specific bits
        var author = authorXPath.evaluate(doc, XPathConstants.STRING).toString()
        author = if (author == "") properties.get("author").toString() else author
        var affiliation = affiliationXPath.evaluate(doc, XPathConstants.STRING).toString()
        affiliation = if (affiliation == "") properties.get("affiliation").toString() else affiliation
        var jobTitle = jobTitleXPath.evaluate(doc, XPathConstants.STRING).toString()
        jobTitle = if (jobTitle == "") properties.get("title").toString() else jobTitle
        logger.info("Author bits: ${author}|${affiliation}|${jobTitle}")


        val contacts = contactXPath.evaluate(doc, XPathConstants.NODESET) as XMLNodeList
        val contactInfo = mutableMapOf<String, String>()
        for (prop in properties) {
            if (prop.key.toString().startsWith("contact.")) {
                val subkey = prop.key.toString().substringAfter("contact.")
                contactInfo[subkey] = prop.value.toString()
            }
        }
        if (contacts.length > 0) {
            for (n in 0..contacts.length-1) {
                val node = contacts.item(n)
                when (node.localName) {
                    "email" -> contactInfo["email"] = node.textContent
                    "blog" -> contactInfo["blog"] = node.textContent
                    "twitter" -> contactInfo["twitter"] = node.textContent
                    "linkedin" -> contactInfo["linkedin"] = node.textContent
                    "github" -> contactInfo["github"] = node.textContent
                    else -> logger.warning("Unrecognized contact info: ${node.localName}/${node.textContent}")
                }
            }
        }

        return Presentation(title, abstract, audience,
                author, affiliation, jobTitle,
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

                    val notesNode = slideNotesXPath.evaluate(node, XPathConstants.NODESET) as XMLNodeList?
                    val notesList = mutableListOf<String>()
                    if (notesNode != null && notesNode.length > 0) {
                        for (n in 0..notesNode.length-1) {
                            val notesNodeChild = notesNode.item(n)
                            notesList.add(notesNodeChild.textContent)
                            node.removeChild(notesNodeChild)
                        }
                    }

                    ast.add(Slide(title, mdParser.parse(node.textContent.trimIndent()), node.textContent, notesList))
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
}
