package com.newardassociates.pptbuilder.slidy

import com.newardassociates.pptbuilder.Presentation
import com.newardassociates.pptbuilder.Processor
import com.newardassociates.pptbuilder.Section
import com.newardassociates.pptbuilder.Slide
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.logging.Logger

/**
 * Documented at https://www.w3.org/2005/03/slideshow.html#(3) (sort of; wish there was better reference docs)
 *
 * TODO: Create a thematic background/colorscheme/images
 * TODO: "templates" (themes) with Slidy
 * TODO: Set up an "offline" option for Slidy; this will write all the files needed into a localized .zip file output
 */
class SlidyProcessor(options: Options) : Processor(options) {
    private val logger = Logger.getLogger(SlidyProcessor::class.java.canonicalName)

    var outputString = ""

    override val processorExtension : String = "html"
    override fun write(outputFilename: String) {
        // Close it all up
        outputString += "\n</body>\n</html>"

        logger.info("Writing contents to ${outputFilename}...")
        FileWriter(outputFilename).use {
            it.write(outputString)
        }
    }

    override fun processPresentationNode(presentation: Presentation) {
        logger.info("processPresentationNode")

        fun contactInfoString() : String {
            val contactInfoLines = mutableListOf<String>()

            presentation.contactInfo.entries.forEach { entry ->
                logger.info("Processing contactinfo $entry")
                if (entry.value.isNotBlank()) {
                    val indent = "                "
                    when (entry.key) {
                        "web" -> contactInfoLines.add("${indent}<a target=\"_blank\" href=\"${entry.value}\">${entry.value}</a>")
                        "blog" -> contactInfoLines.add("${indent}<a target=\"_blank\" href=\"${entry.value}\">Blog: ${entry.value}</a>")
                        "email" -> contactInfoLines.add("${indent}<a href=\"mailto:${entry.value}\">${entry.value}</a>")
                        "twitter" -> contactInfoLines.add("${indent}<a target=\"_blank\" href=\"https://twitter.com/${entry.value}\">Twitter: ${entry.value}</a>")
                        "linkedin" -> contactInfoLines.add("${indent}<a target=\"_blank\" href=\"https://www.linkedin.com/in/${entry.value}/\">LinkedIn: ${entry.value}</a>")
                        "github" -> contactInfoLines.add("${indent}<a target=\"_blank\" href=\"https://github.com/${entry.value}\">Github: ${entry.value}</a>")
                    }
                }
            }

            return contactInfoLines.joinToString(" | ")
        }

        val templateScript = if (options.templateFile != "") "<link rel=\"stylesheet\" href=\"${options.templateFile}\" type=\"text/css\" />" else ""
        outputString += """
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"> 
<head>
    <title>${presentation.title.replace("|", " ")}</title>
    <meta name="copyright" content="Copyright &#169; ${SimpleDateFormat("yyyy").format(Date.from(Instant.now()))} ${if (presentation.affiliation.isEmpty()) presentation.author else presentation.affiliation}" /> 
    <link rel="stylesheet" type="text/css" media="screen, projection, print" href="http://www.w3.org/Talks/Tools/Slidy2/styles/slidy.css" /> 
    <script src="http://www.w3.org/Talks/Tools/Slidy2/scripts/slidy.js" charset="utf-8" type="text/javascript"></script> 
    ${templateScript}
</head>
<body>

<div class="slide cover">
    <h1>${presentation.title.replace("|", "\n")}</h1>
    <p>${contactInfoString()}</p>
</div>
"""
    }

    override fun processSection(section: Section) {
        outputString += """
<div class="slide cover">
    <h1>${section.title}</h1>
    <h2>${section.subtitle ?: (section.quote + " --" + section.attribution)}</h2>
</div>
"""
    }

    override fun processContentSlide(slide: Slide) {
        logger.info("Creating content slide for $slide")

        outputString += "\n<div class=\"slide\">\n<h1>${slide.title}</h1>\n"

        val visitor = NodeVisitor()

        // This current implementation means that headings can't have anything other
        // than raw text as children--all formatting will be ignored
        visitor.addHandler(VisitHandler<Heading>(Heading::class.java, fun (head : Heading) {
            outputString += "<h2>${head.text}</h2>\n"
        }))

        visitor.addHandler(VisitHandler(BulletList::class.java, fun (bl : BulletList) {
            outputString += "<ul>\n"
            visitor.visitChildren(bl)
            outputString += "</ul>\n"
        }))
        visitor.addHandler(VisitHandler(OrderedList::class.java, fun (ol : OrderedList) {
            outputString += "<ol>\n"
            visitor.visitChildren(ol)
            outputString += "</ol>\n"
        }))
        visitor.addHandler(VisitHandler(ListItem::class.java, fun (li : ListItem) {
            outputString += "<li>"
            visitor.visitChildren(li)
            outputString += "</li>\n"
        }))
        visitor.addHandler(VisitHandler(Paragraph::class.java, fun(p : Paragraph) {
            outputString += "<p>\n"
            visitor.visitChildren(p)
            outputString += "</p>\n"
        }))
        visitor.addHandler(VisitHandler(Emphasis::class.java, fun (em : Emphasis) {
            outputString += "<em>"
            visitor.visitChildren(em)
            outputString += "</em>"
        }))
        visitor.addHandler(VisitHandler(Code::class.java, fun (em : Code) {
            outputString += "<code>"
            visitor.visitChildren(em)
            outputString += "</code>"
        }))
        visitor.addHandler(VisitHandler(StrongEmphasis::class.java, fun (em : StrongEmphasis) {
            outputString += "<strong>"
            visitor.visitChildren(em)
            outputString += "</strong>"
        }))
        visitor.addHandler(VisitHandler(Text::class.java, fun (t : Text) {
            outputString += t.chars.unescape()
        }))

        visitor.visit(slide.markdownBody)

        outputString += "</div>\n"
    }

    override fun processLegacyCodeSlide(slide: Slide) {
        fun escapeHTMLSensitiveChars(string : String) : String {
            return string.replace("&", "&amp;").
                replace("<", "&lt;")
        }

        logger.info("Creating legacy code slide for $slide")
        outputString += "<div class=\"slide\">\n"
        outputString += "<h1>${slide.title}</h1>\n"

        val childNodes = slide.node.childNodes
        for (nidx in 0 until childNodes.length) {
            val node = childNodes.item(nidx)
            when (node.nodeName) {
                "text" -> {
                    logger.info("Handling text section: " + node.textContent)
                    outputString += "<p>${escapeHTMLSensitiveChars(node.textContent)}</p>"
                }
                "code" -> {
                    logger.info("Handling code section: " + node.textContent)
                    outputString += "<pre>\n${escapeHTMLSensitiveChars(importCode(node))}\n</pre>"
                }
            }
        }
        outputString += "\n</div>\n"
    }
}
