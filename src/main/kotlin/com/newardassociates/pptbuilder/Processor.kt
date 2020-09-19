package com.newardassociates.pptbuilder

import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Paths
import java.util.logging.Logger

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

    open fun process(presentation : Presentation) {
        processPresentationNode(presentation)
        for (slide in presentation.slides) {
            when (slide) {
                is Slide -> processSlide(slide)
                is Section -> processSection(slide)
            }
        }

        write(options.outputFilename + (if (options.outputFilename.endsWith(processorExtension)) "" else processorExtension))
    }

    abstract fun processPresentationNode(presentation : Presentation)
    abstract fun processSection(section : Section)
    abstract fun processSlide(slide : Slide)

    fun importCode(node : Node) : String {
        logger.info("Importing code")

        // Are we importing code from disk, or using what's inside the code tag itself?
        if (node.hasAttributes() && node.attributes.getNamedItem("src") != null) {
            // Importing code from disk; find out the src and the optional marker

            // If this file was xinclude'd, then the relative location of the src file
            // won't be to correct; grab the baseURI of the xinclude'd document to use
            // that as a starting point for finding the file in question
            val src = node.attributes.getNamedItem("src").textContent
            val srcfile = if (node.baseURI != null) {
                Paths.get(URI.create(node.baseURI)).parent.resolve(src).toFile()
            } else {
                File(src)
            }
            val marker = node.attributes.getNamedItem("marker")?.textContent
            logger.info("Importing code from ${srcfile}${if (marker != null) " using marker '$marker'" else ""}")

            if (!srcfile.exists()) {
                logger.warning("${srcfile.absolutePath} does not appear to exist")
                return "<<${srcfile.absolutePath} NOT FOUND>>"
            }
            else {
                val text = mutableListOf<String>()
                if (marker == null) {
                    srcfile.forEachLine { text.add(it) }
                } else {
                    var capturing = false
                    srcfile.forEachLine {
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
            }
        } else {
            // Using what's inside the code tag itself
            logger.info("Using body of code tag")
            return node.textContent
        }
    }
}
