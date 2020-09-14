package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.*
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler
import org.apache.poi.sl.usermodel.AutoNumberingScheme
import org.apache.poi.sl.usermodel.PaintStyle
import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.xslf.usermodel.XSLFTextParagraph
import org.apache.poi.xslf.usermodel.XSLFTextRun
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.w3c.dom.NodeList
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileOutputStream
import java.util.*
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
fun <T> MutableList<T>.hasMore() = this.count() > 0
typealias Stack<T> = MutableList<T>

// Things to do:
// -- start from or associate to PPTX template
class PPTXProcessor(options : Options) : Processor(options) {
    val logger = Logger.getLogger("PPTXProcessor")
    val deck = Deck()
    var currentSlide: pptxSlide? = null
    var currentPara: XSLFTextParagraph? = null
    var currentRun: XSLFTextRun? = null

    override fun process(presentation: Presentation) {
        logger.info("Beginning PPTX processing of ${presentation.title} to ${options.outputFilename}")
        super.process(presentation)

        logger.info("Writing contents to file...")
        deck.ppt.write(FileOutputStream(options.outputFilename))
    }

    override fun processPresentationNode(presentation: Presentation) {
        logger.info("Processing presentation node...")
        val titleSlide = Title(deck, presentation.title.replace("|", "\n"), "")

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

        deck.title = presentation.title.replace("|", "\n")
        deck.author = authorText
        deck.keywords = presentation.keywords.joinToString()
        deck.description = presentation.abstract.trimIndent() + "\n" +
                "Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)} ${presentation.author}"
    }

    override fun processSection(section: Section) {
        logger.info("Creating section ${section.title} slide...")
        val sh = if (section.subtitle != null && section.subtitle.isNotBlank())
            SectionHeader(deck, section.title, section.subtitle)
        else
            SectionHeader(deck, section.title, section.quote.orEmpty() + "\n --" + section.attribution.orEmpty())
    }

    private val xpath: XPath = XPathFactory.newInstance().newXPath()
    private val codeXPath = xpath.compile(".//code")
    override fun processSlide(slide : Slide) {
        val codeNodes = codeXPath.evaluate(slide.node, XPathConstants.NODESET) as NodeList?
        if (codeNodes != null && codeNodes.length > 0) {
            processLegacyCodeSlide(slide)
        }
        else {
            processContentSlide(slide)
        }
    }

    fun processLegacyCodeSlide(slide : Slide) {
        logger.info("Creating legacy code slide for $slide")
        val currentSlide = TitleOnly(deck, slide.title)

        logger.info("Title anchor: ${currentSlide.title.anchor}")
        val titleAnchor = currentSlide.title.anchor
        val currentAnchor = Rectangle2D.Double(titleAnchor.x, titleAnchor.y + titleAnchor.height, titleAnchor.width, 0.0)
        println("currentAnchor = $currentAnchor")

        val childNodes = slide.node.childNodes
        for (nidx in 0..childNodes.length - 1) {
            val node = childNodes.item(nidx)
            when (node.nodeName) {
                "text" -> {
                    logger.info("Handling text section: " + node.textContent)

                    val newShape = currentSlide.slide.createTextBox()
                    newShape.anchor = Rectangle2D.Double(currentAnchor.x, currentAnchor.y, currentAnchor.width, 0.0)
                    newShape.clearText()

                    val paragraph = newShape.addNewTextParagraph()
                    paragraph.fontAlign = TextParagraph.FontAlign.TOP
                    val run = paragraph.addNewTextRun()
                    run.setText(node.textContent)
                    newShape.resizeToFitText()

                    currentAnchor.y += newShape.anchor.height
                }
                "code" -> {
                    logger.info("Handling code section: " + node.textContent)

                    val newShape = currentSlide.slide.createTextBox()
                    newShape.anchor = Rectangle2D.Double(currentAnchor.x, currentAnchor.y, currentAnchor.width, 0.0)
                    newShape.clearText()

                    val paragraph = newShape.addNewTextParagraph()
                    paragraph.setBulletStyle() // omit bullets
                    paragraph.fontAlign = TextParagraph.FontAlign.TOP
                    val run = paragraph.addNewTextRun()
                    run.fontFamily = "Consolas"
                    run.fontSize = 14.0
                    run.setFontColor(Color.WHITE)
                    newShape.fillColor = Color.BLACK

                    // Are we importing code from disk, or using what's inside the code tag itself?
                    if (node.hasAttributes() && node.attributes.getNamedItem("src") != null) {
                        // Importing code from disk; find out the src and the optional marker
                        val srcfile = node.attributes.getNamedItem("src").textContent
                        val marker = node.attributes.getNamedItem("marker")?.textContent
                        logger.info("Pulling code from $srcfile at $marker")

                        val file = File(srcfile)
                        logger.info("file: ${file.absoluteFile} : ${file.isFile}")
                        val text = mutableListOf<String>()
                        if (marker == null) {
                            logger.info("No marker; grabbing the entire file")
                            file.forEachLine { text.add(it) }
                        }
                        else {
                            logger.info("Scanning for marker ${marker}")
                            var capturing = false
                            file.forEachLine {
                                if (it.contains("{{## END " + marker + " ##}}"))
                                    capturing = false
                                if (capturing)
                                    text.add(it)
                                if (it.contains("{{## BEGIN " + marker + " ##}}"))
                                    capturing = true
                            }
                        }

                        logger.info("Imported code: $text")

                        val setText = text.joinToString("\n")
                        run.setText(setText)
                    }
                    else {
                        // Using what's inside the code tag itself
                        run.setText(node.textContent)
                    }

                    newShape.resizeToFitText()

                    currentAnchor.y += newShape.anchor.height
                }
            }
        }
    }
    fun processContentSlide(slide : Slide) {
        logger.info("Creating content slide for $slide")
        val currentSlide = TitleAndContent(deck, slide.title)
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
                val para = currentSlide.content.addNewTextParagraph()
                para.isBullet = true
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
                logger.info("Ordered List! indent level ${indentLevel}")
                val para = currentSlide.content.addNewTextParagraph()
                para.isBullet = true
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

        visitor.visit(slide.markdownBody)
    }
    override fun startEmphasis(em: Emphasis) {
        if (currentRun == null)
            currentRun = currentPara!!.addNewTextRun()
        currentRun!!.isItalic = true
    }

    override fun endEmphasis(em: Emphasis) {
        currentRun = null
    }

    override fun startStrongEmphasis(em: StrongEmphasis) {
        if (currentRun == null)
            currentRun = currentPara!!.addNewTextRun()
        currentRun!!.isBold = true
    }
    override fun endStrongEmphasis(em: StrongEmphasis) {
        currentRun = null
    }

    override fun startCode(code : Code) {
    }
    override fun endCode(code: Code) {
    }

    override fun text(text : Text) {
        if (currentRun == null)
            currentRun = currentPara!!.addNewTextRun()
        currentRun!!.setText(text.childChars.unescape())
    }
}
