package com.newardassociates.pptbuilder.reveal

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

class RevealProcessor(options: Options) : Processor(options) {
    private val logger = Logger.getLogger(RevealProcessor::class.java.canonicalName)

    var outputString = ""

    override val processorExtension : String = "html"
    override fun write(outputFilename: String) {
        // Close it all up
        outputString += """
        </div>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.9.2/js/reveal.js"></script>
        <script>Reveal.initialize({ slideNumber: true });</script>
    </body>
</html>
        """.trimIndent()

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

            return contactInfoLines.joinToString("\n")
        }

        outputString += """
<html>
    <head>
        <title>${presentation.title.replace("|", " ")}</title>
        <meta name="author" content="${presentation.author}" />
        <meta name="copyright" content="Copyright &#169; ${SimpleDateFormat("yyyy").format(Date.from(Instant.now()))} ${if (presentation.affiliation.isEmpty()) presentation.author else presentation.affiliation}" /> 
        <meta name="description" content="${presentation.abstract}" />
        <meta name="keywords" content="${presentation.keywords.joinToString(",")}" />

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.9.2/css/reveal.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.9.2/css/theme/black.min.css">
    </head>
    <body>
        <div class="reveal">
        <div class="slides">
        <section>
            <h1>${presentation.title.replace("|", "\n")}</h1>
            <p>
${contactInfoString()}
            </p>
        </section>
        """
    }

    override fun processSection(section: Section) {
        outputString += """
        <section>
            <h1>${section.title}</h1>
            <h2>${section.subtitle ?: section.quote + " --" + section.attribution}</h2>
        </section>
        """
    }

    override fun processContentSlide(slide: Slide) {
        logger.info("Creating content slide for $slide")

        outputString += "        <section>        <h1>${slide.title}</h1>"

        val visitor = NodeVisitor()

        // This current implementation means that headings can't have anything other
        // than raw text as children--all formatting will be ignored
        visitor.addHandler(VisitHandler<Heading>(Heading::class.java, fun (head : Heading) {
            outputString += "            <h2>${head.text}</h2>\n"
        }))

        visitor.addHandler(VisitHandler<BulletList>(BulletList::class.java, fun (bl : BulletList) {
            outputString += "<ul>\n"
            visitor.visitChildren(bl)
            outputString += "</ul>\n"
        }))
        visitor.addHandler(VisitHandler<OrderedList>(OrderedList::class.java, fun (ol : OrderedList) {
            outputString += "<ol>\n"
            visitor.visitChildren(ol)
            outputString += "</ol>\n"
        }))
        visitor.addHandler(VisitHandler<ListItem>(ListItem::class.java, fun (li : ListItem) {
            outputString += "<li>"
            visitor.visitChildren(li)
            outputString += "</li>\n"
        }))
        visitor.addHandler(VisitHandler<Paragraph>(Paragraph::class.java, fun(p : Paragraph) {
            outputString += "<p>\n"
            visitor.visitChildren(p)
            outputString += "</p>\n"
        }))
        visitor.addHandler(VisitHandler<Emphasis>(Emphasis::class.java, fun (em : Emphasis) {
            outputString += "<em>"
            visitor.visitChildren(em)
            outputString += "</em>"
        }))
        visitor.addHandler(VisitHandler<Code>(Code::class.java, fun (em : Code) {
            outputString += "<code>"
            visitor.visitChildren(em)
            outputString += "</code>"
        }))
        visitor.addHandler(VisitHandler<StrongEmphasis>(StrongEmphasis::class.java, fun (em : StrongEmphasis) {
            outputString += "<strong>"
            visitor.visitChildren(em)
            outputString += "</strong>"
        }))
        visitor.addHandler(VisitHandler<Text>(Text::class.java, fun (t : Text) {
            outputString += t.chars.unescape()
        }))

        visitor.visit(slide.markdownBody)

        outputString += "            </section>"
    }

    override fun processLegacyCodeSlide(slide: Slide) {
        logger.info("Creating legacy code slide for $slide")
        outputString += "        <section>        <h1>${slide.title}</h1>"

        val childNodes = slide.node.childNodes
        for (nidx in 0 until childNodes.length) {
            val node = childNodes.item(nidx)
            when (node.nodeName) {
                "text" -> {
                    logger.info("Handling text section: " + node.textContent)
                    outputString += "            <p>${node.textContent}</p>"
                }
                "code" -> {
                    logger.info("Handling code section: " + node.textContent)
                    outputString += "            <pre style=\"background-color:black\">\n${importCode(node)}\n</pre>"
                }
            }
        }

        outputString += "            </section>"
    }
}
