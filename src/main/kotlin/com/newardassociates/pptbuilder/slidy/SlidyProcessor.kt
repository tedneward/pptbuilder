package com.newardassociates.pptbuilder.slidy

import com.ibm.icu.text.DateFormat
import com.newardassociates.pptbuilder.Presentation
import com.newardassociates.pptbuilder.Processor
import com.newardassociates.pptbuilder.Section
import com.newardassociates.pptbuilder.Slide
import com.newardassociates.pptbuilder.pptx.PPTXProcessor
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.logging.Logger

class SlidyProcessor(options: Options) : Processor(options) {
    private val logger = Logger.getLogger(SlidyProcessor::class.java.canonicalName)

    var outputString = ""

    override fun process(presentation: Presentation) {
        super.process(presentation)

        outputString += "\n</body>\n</html>"
    }

    override val processorExtension : String = "html"
    override fun write(outputFilename: String) {
        logger.info("Writing contents to ${outputFilename}...")

        FileWriter(options.outputFilename).use {
            it.write(outputString)
        }
    }

    override fun processPresentationNode(presentation: Presentation) {
        logger.info("processPresentationNode")

        fun contactInfoString() : String {
            val contactInfoLines = mutableListOf<String>()

            presentation.contactInfo.entries.forEach { entry ->
                logger.info("Processing contactinfo ${entry}")
                if (entry.value.isNotBlank()) {
                    val indent = "                "
                    when (entry.key) {
                        "blog" -> contactInfoLines.add("${indent}<a href=\"${entry.value}\">Blog: ${entry.value}</a>")
                        "email" -> contactInfoLines.add("${indent}<a href=\"mailto:${entry.value}\">${entry.value}</a>")
                        "twitter" -> contactInfoLines.add("${indent}<a href=\"http://twitter.org/${entry.value}\">Twitter: ${entry.value}</a>")
                        "linkedin" -> contactInfoLines.add("${indent}<a href=\"http://linkedin.com/${entry.value}\">LinkedIn: ${entry.value}</a>")
                        "github" -> contactInfoLines.add("${indent}<a href=\"https://github.com/${entry.value}\">Github: ${entry.value}</a>")
                    }
                }
            }

            return contactInfoLines.joinToString("\n")
        }

        outputString += """
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"> 
    <head>
        <title>${presentation.title}</title>
        <meta name="author" content="${presentation.author}" />
        <meta name="copyright" content="Copyright &#169; ${SimpleDateFormat("yyyy").format(Date.from(Instant.now()))} ${presentation.author}" /> 
        <meta name="description" content="${presentation.abstract}" />
        <meta name="keywords" content="${presentation.keywords.joinToString(",")}" />
        <link rel="stylesheet" type="text/css" media="screen, projection" href="http://www.w3.org/Talks/Tools/Slidy/show.css" />
        <link rel="stylesheet" type="text/css" media="screen, projection" href="http://www.w3.org/Talks/Tools/Slidy/w3c-blue.css" />
        <script src="http://www.w3.org/Talks/Tools/Slidy/slidy.js" type="text/javascript"></script> 
        <style type="text/css"> 
            <!-- your custom style rules --> 
        </style> 
        <link rel="stylesheet" type="text/css" media="print" href="http://www.w3.org/Talks/Tools/Slidy/print.css" /> 
    </head>
    
    <body>
        <div class="background"> 
            <img id="head-icon" alt="graphic with four colored squares" src="http://www.w3.org/Talks/Tools/Slidy/icon-blue.png" align="left" /> 
            <object id="head-logo" title="W3C logo" type="image/svg+xml" data="http://www.w3.org/Talks/Tools/Slidy/w3c-logo.svg">
                <img alt="W3C logo" id="head-logo-fallback" src="http://www.w3.org/Talks/Tools/Slidy/w3c-logo-blue.gif" align="right"/>
            </object> 
        </div> 
        <div class="slide cover">
            <img src="http://www.w3.org/Talks/Tools/Slidy/keys.jpg" alt="Cover page images" class="cover" />
            <br clear="all" />
            <h1>${presentation.title.replace("|", "\n")}</h1>
            <p>
${contactInfoString()}
            </p>
        </div>
        """
    }

    override fun processSection(section: Section) {
        outputString += """
        <div class="slide cover">
            <img src="http://www.w3.org/Talks/Tools/Slidy/keys.jpg" alt="Cover page images (keys)" class="cover" />
            <br clear="all" />            
            <h1>${section.title}</h1>
            <h2>${if (section.subtitle != null) section.subtitle else section.quote + " --" + section.attribution}</h2>
        </div>
        """
    }

    override fun processSlide(slide: Slide) {
        outputString += """
        <div class="slide">
            <h1>${slide.title}</h1>
        </div>
        """
    }
}
