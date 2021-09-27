package com.newardassociates.pptbuilder.pptx

import com.newardassociates.pptbuilder.*
import com.newardassociates.pptbuilder.pptx.*
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ext.footnotes.Footnote
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler
import org.apache.poi.sl.usermodel.AutoNumberingScheme
import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextParagraph
import org.apache.poi.xslf.usermodel.XSLFTextRun
import org.w3c.dom.NodeList
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Paths
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Logger
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import com.newardassociates.pptbuilder.pptx.Slide as pptxSlide

/*
Stack implementation on top of MutableList<T>
 */
fun <T> MutableList<T>.push(item: T) = this.add(this.count(), item)
fun <T> MutableList<T>.pop(): T? = if (this.count() > 0) this.removeAt(this.count() - 1) else null
fun <T> MutableList<T>.peek(): T? = if (this.count() > 0) this[this.count() - 1] else null
typealias Stack<T> = MutableList<T>

// Things to do:
// -- start from or associate to PPTX template
class PPTXProcessor(options : Options) : Processor(options) {
    private val logger = Logger.getLogger(PPTXProcessor::class.java.canonicalName)
    private val deck = Deck(if (options.templateFile != "") XMLSlideShow(FileInputStream(options.templateFile)) else XMLSlideShow())

    override val processorExtension : String = "pptx"
    override fun write(outputFilename: String) {
        logger.info("Writing contents to ${outputFilename}...")
        deck.ppt.write(FileOutputStream(outputFilename))
    }

    override fun processPresentationNode(presentation: Presentation) {
        logger.info("Processing presentation node...")
        val titleSlide = TitleSlide(deck, presentation.title.replace("|", "\n"), "")

        // Subtitle text -- author, affiliation, and contact information
        val authorText = "${presentation.author}\n" +
                when {
                    presentation.jobTitle.isNotBlank() &&
                            presentation.affiliation.isNotBlank() -> "${presentation.jobTitle}, ${presentation.affiliation}"
                    presentation.jobTitle.isNotBlank() -> presentation.jobTitle
                    presentation.affiliation.isNotBlank() -> presentation.affiliation
                    else -> ""
                }
        titleSlide.subtitle.text = authorText

        // Contact paragraph (email | blog | twitter | linkedIn)
        val contactPara = titleSlide.subtitle.addNewTextParagraph()
        val contactFontSize = (contactPara.defaultFontSize * 3) / 4

        if (presentation.contactInfo["email"] != null && presentation.contactInfo["email"]!!.isNotEmpty()) {
            val run = contactPara.addNewTextRun()
            run.fontSize = contactFontSize
            run.setText(presentation.contactInfo["email"])
            val link = run.createHyperlink()
            link.linkToEmail(presentation.contactInfo["email"])
        }

        if (presentation.contactInfo["blog"] != null && presentation.contactInfo["blog"]!!.isNotEmpty()) {
            contactPara.addNewTextRun().setText(" ")

            val run = contactPara.addNewTextRun()
            run.fontSize = contactFontSize
            run.setText(presentation.contactInfo["blog"] + " ")
            val link = run.createHyperlink()
            link.linkToUrl(presentation.contactInfo["blog"])
        }

        if (presentation.contactInfo["twitter"] != null && presentation.contactInfo["twitter"]!!.isNotEmpty()) {
            contactPara.addNewTextRun().setText(" ")

            val run = contactPara.addNewTextRun()
            run.fontSize = contactFontSize
            run.setText("@${presentation.contactInfo["twitter"]}")  // twitter icon Unicode
            val twitterLink = run.createHyperlink()
            twitterLink.linkToUrl("http://twitter.com/" + presentation.contactInfo["twitter"])
        }

        // TODO: Other contact info displayed: LinkedIn, Github

        deck.title = presentation.title.replace("|", " ")
        deck.author = authorText
        deck.affiliation = "Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)} ${presentation.author}"
        deck.keywords = presentation.keywords.joinToString()
        deck.subject = presentation.keywords.joinToString()
        deck.description = presentation.abstract.trimIndent() + "\n"
    }

    override fun processSection(section: Section) {
        logger.info("Creating section ${section.title} slide...")
        if (section.subtitle != null && section.subtitle.isNotBlank())
            SectionHeaderSlide(deck, section.title, section.subtitle)
        else
            SectionHeaderSlide(deck, section.title, section.quote.orEmpty() + "\n --" + section.attribution.orEmpty())
    }

    override fun processLegacyCodeSlide(slide : Slide) {
        logger.info("Creating legacy code slide for $slide")
        val currentSlide = CodeSlide(deck, slide.title)

        logger.info("Title anchor: ${currentSlide.title.anchor}")
        //val titleAnchor = currentSlide.title.anchor

        val childNodes = slide.node.childNodes
        for (nidx in 0 until childNodes.length) {
            val node = childNodes.item(nidx)
            when (node.nodeName) {
                "text" -> {
                    logger.info("Handling text section: " + node.textContent)
                    currentSlide.addTextBlock(node.textContent)
                }
                "code" -> {
                    logger.info("Handling code section: " + node.textContent)
                    currentSlide.addCodeBlock(importCode(node))
                }
            }
        }
    }

    override fun processContentSlide(slide : Slide) {
        logger.info("Creating content slide for $slide")
        val currentSlide = TitleAndContentSlide(deck, slide.title)
        val paragraphStack : Stack<XSLFTextParagraph> = mutableListOf()
        val paragraphGeneratorStack : Stack<() -> XSLFTextParagraph> = mutableListOf()
        paragraphGeneratorStack.push {
            logger.info("New paragraph, no indent")
            val para = currentSlide.content.addNewTextParagraph()
            para.isBullet = false
            para
        }

        // Process slide notes

        val visitor = NodeVisitor()

        // This current implementation means that headings can't have anything other
        // than raw text as children--all formatting will be ignored
        visitor.addHandler(VisitHandler<Heading>(Heading::class.java, fun (head : Heading) {
            currentSlide.header(head.childChars.unescape())
        }))

        // Bulleted lists, bulleted list items
        visitor.addHandler(VisitHandler<BulletList>(BulletList::class.java, fun (bl : BulletList) {
            val indentLevel = paragraphGeneratorStack.size - 1
            // -1 for the default paragraphGenerator
            paragraphGeneratorStack.push {
                logger.info("Bulleted List! indent level $indentLevel")
                val para = currentSlide.content.addNewTextParagraph()
                para.indentLevel = indentLevel
                para
            }
            currentSlide.newList()

            visitor.visitChildren(bl)

            currentSlide.endList()
            paragraphGeneratorStack.pop()
        }))

        // Ordered lists, ordered list items
        visitor.addHandler(VisitHandler<OrderedList>(OrderedList::class.java, fun (ol : OrderedList) {
            val indentLevel = paragraphGeneratorStack.size - 1
            // -1 for the default paragraphGenerator
            paragraphGeneratorStack.push {
                logger.info("Ordered List! indent level $indentLevel")
                val para = currentSlide.content.addNewTextParagraph()
                para.indentLevel = indentLevel
                para.setBulletAutoNumber(AutoNumberingScheme.arabicPeriod, 1)
                para
            }
            currentSlide.newList()

            visitor.visitChildren(ol)

            currentSlide.endList()
            paragraphGeneratorStack.pop()
        }))

        // There's always a paragraph wrapping whatever text we see
        visitor.addHandler(VisitHandler<Paragraph>(Paragraph::class.java, fun(p : Paragraph) {
            paragraphStack.push((paragraphGeneratorStack.peek()!!)())

            visitor.visitChildren(p)

            paragraphStack.pop()
        }))
        visitor.addHandler(VisitHandler<Text>(Text::class.java, fun (t : Text) {
            val run = paragraphStack.peek()!!.addNewTextRun()
            run.setText(t.chars.unescape())
        }))
        visitor.addHandler(VisitHandler<Emphasis>(Emphasis::class.java, fun (em : Emphasis) {
            val run = paragraphStack.peek()!!.addNewTextRun()
            run.isItalic = true
            run.setText(em.childChars.unescape())
        }))
        visitor.addHandler(VisitHandler<Code>(Code::class.java, fun (em : Code) {
            val run = paragraphStack.peek()!!.addNewTextRun()
            run.fontFamily = "Courier New"
            run.setText(em.childChars.unescape())
        }))
        visitor.addHandler(VisitHandler<StrongEmphasis>(StrongEmphasis::class.java, fun (em : StrongEmphasis) {
            val run = paragraphStack.peek()!!.addNewTextRun()
            run.isBold = true
            run.setText(em.childChars.unescape())
        }))
        visitor.addHandler(VisitHandler<Footnote>(Footnote::class.java, fun (fn : Footnote) {
            logger.info("FOOTNOTE: ${fn.referenceOrdinal}, ${fn.reference} ${fn.footnoteBlock} (${fn})")
            val run = paragraphStack.peek()!!.addNewTextRun()
            run.isSuperscript = true
            run.setText("[${fn.reference.unescape()}]")
            footnotes[fn.reference.unescape()] = ""
        }))

        visitor.visit(slide.markdownBody)
    }
}
