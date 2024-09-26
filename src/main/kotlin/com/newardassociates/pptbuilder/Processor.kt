package com.newardassociates.pptbuilder

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.nio.file.Paths
import java.util.logging.Logger
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

abstract class Processor(val options : Options) {
    private val logger = Logger.getLogger(Processor::class.java.canonicalName)

    data class Options(
        val outputFilename : String = "",
        val templateFile : String = "",
        val baseDirectory : String = "",
        val noTitleSlide : Boolean = false,
        val noNotesSlides : Boolean = false
    )

    abstract val processorExtension : String

    abstract fun write(outputFilename : String)

    protected val footnotes = mutableMapOf<String, String>()

    open fun process(presentation : Presentation) {
        processPresentationNode(presentation)
        for (slide in presentation.slides) {
            when (slide) {
                is Slide -> processSlide(slide)
                is Section -> processSection(slide)
            }
        }

        processReferencesSection()

        write(options.outputFilename + (if (options.outputFilename.endsWith(processorExtension)) "" else ".$processorExtension"))
    }

    abstract fun processPresentationNode(presentation : Presentation)
    abstract fun processSection(section : Section)
    open fun processReferencesSection() { }
    open fun unrecognizedTag(tagname : String, tagbody : String) { logger.info("Unrecongized tag: $tagname ($tagbody)") }


    private val xpath: XPath = XPathFactory.newInstance().newXPath()
    private val codeXPath = xpath.compile(".//code")
    open fun processSlide(slide : Slide) {
        val codeNodes = codeXPath.evaluate(slide.node, XPathConstants.NODESET) as NodeList?
        if (codeNodes != null && codeNodes.length > 0) {
            processLegacyCodeSlide(slide)
        }
        else {
            processContentSlide(slide)
        }
    }

    open fun processLegacyCodeSlide(slide : Slide) { }
    open fun processContentSlide(slide : Slide) { }

    fun importCode(node : Node) : String {
        logger.info("Importing code")

        // Are we importing code from disk, or using what's inside the code tag itself?
        if (node.hasAttributes() && node.attributes.getNamedItem("src") != null) {
            // Importing code from disk; find out the src and the optional marker

            // If this file was xinclude'd, then the relative location of the src file
            // won't be to correct; grab the baseURI of the xinclude'd document to use
            // that as a starting point for finding the file in question
            val src = node.attributes.getNamedItem("src").textContent
            val filetext = mutableListOf<String>()
            val marker = node.attributes.getNamedItem("marker")?.textContent
            logger.info("Importing code from ${src}${if (marker != null) " using marker '$marker'" else ""}")

            if (src.startsWith("http://") || src.startsWith("https://")) {
                // It's a URL, not a file path
                val url = URI(src).toURL()
                try {
                    val reader = BufferedReader(InputStreamReader(url.openStream()))
                    reader.lines().forEach { filetext.add(it) }
                }
                catch (ioEx: IOException) {
                    logger.warning("${url} does not appear to exist")
                    return "<<${url} NOT FOUND>>"
                }
            }
            else {
                // It's a file path
                val srcfile = if (node.baseURI != null) {
                    Paths.get(URI.create(node.baseURI)).parent.resolve(src).toFile()
                } else {
                    File(src)
                }

                if (!srcfile.exists()) {
                    logger.warning("${srcfile.absolutePath} does not appear to exist")
                    return "<<${srcfile.absolutePath} NOT FOUND>>"
                }
                else {
                    srcfile.forEachLine { filetext.add(it) }
                }
            }

            val text = mutableListOf<String>()
            if (marker == null) {
                filetext.forEach { text.add(it) }
            } else {
                var capturing = false
                filetext.forEach {
                    if (it.contains("{{## END $marker ##}}"))
                        capturing = false
                    if (capturing)
                        text.add(if (it == "") "    " else it)
                    if (it.contains("{{## BEGIN $marker ##}}"))
                        capturing = true
                }
            }
            logger.info("Extracted ${text} for slide")
            return text.joinToString("\n")

        } else {
            // Using what's inside the code tag itself
            logger.info("Using body of code tag")
            return node.textContent
        }
    }
}
