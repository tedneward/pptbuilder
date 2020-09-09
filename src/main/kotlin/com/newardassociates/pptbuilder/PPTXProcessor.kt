package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.Deck
import com.newardassociates.pptbuilder.pptx.SectionHeader
import com.newardassociates.pptbuilder.pptx.Title
import com.newardassociates.pptbuilder.pptx.TitleAndContent
import com.vladsch.flexmark.ast.*
import org.apache.poi.xslf.usermodel.XSLFTextParagraph
import org.apache.poi.xslf.usermodel.XSLFTextRun
import java.io.FileOutputStream
import java.util.*
import java.util.logging.Logger
import com.newardassociates.pptbuilder.pptx.Slide as pptxSlide


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

        println("sh = " + sh)
    }

    override fun processSlide(slide : Slide) {
        logger.info("Creating slide for ${slide}")
        currentSlide = TitleAndContent(deck, slide.title)

        super.processSlide(slide)
    }

    override fun heading(head : Heading) {
        (currentSlide!! as TitleAndContent).header(head.childChars.unescape())
    }

    override fun startBulletList(blist: BulletList) {
        (currentSlide!! as TitleAndContent).newList()
    }

    override fun startBulletListItem(blistitem: BulletListItem) {
        currentPara = (currentSlide!! as TitleAndContent).newBulletListItem()
    }

    override fun endBulletListItem(blistitem: BulletListItem) {
        currentPara = null
    }

    override fun endBulletList(blist: BulletList) {
        (currentSlide!! as TitleAndContent).endBulletList()
    }

    override fun startOrderedListItem(olistitem: OrderedListItem) {}
    override fun endOrderedListItem(olistitem: OrderedListItem) {}

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
