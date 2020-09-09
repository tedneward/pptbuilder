package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.Deck
import com.newardassociates.pptbuilder.pptx.SectionHeader
import com.newardassociates.pptbuilder.pptx.Title
import com.newardassociates.pptbuilder.pptx.TitleAndContent
import com.vladsch.flexmark.ast.*
import java.io.FileOutputStream
import java.util.*
import java.util.logging.Logger


// Things to do:
// -- start from or associate to PPTX template
class PPTXProcessor(options : Options) : Processor(options) {
    val logger = Logger.getLogger("PPTXProcessor")
    val deck = Deck()

    override fun process(presentation: Presentation) {
        logger.info("Beginning PPTX processing of ${presentation.title} to ${options.outputFilename}")
        super.process(presentation)
    }

    override fun processPresentationNode(presentation: Presentation) {
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

        if (presentation.contactInfo["email"] != null) {
            val run = contactPara.addNewTextRun()
            run.fontSize = contactFontSize
            run.setText(presentation.contactInfo["email"])
            val link = run.createHyperlink()
            link.linkToEmail(presentation.contactInfo["email"])
        }

        if (presentation.contactInfo["blog"] != null) {
            contactPara.addNewTextRun().setText(" ")

            val run = contactPara.addNewTextRun()
            run.fontSize = contactFontSize
            run.setText(presentation.contactInfo["blog"] + " ")
            val link = run.createHyperlink()
            link.linkToUrl(presentation.contactInfo["blog"])
        }

        if (presentation.contactInfo["twitter"] != null) {
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

        deck.ppt.write(FileOutputStream(options.outputFilename))
    }

    override fun processSection(section: Section) {
        SectionHeader(deck, section.title, section.quote.orEmpty())
    }

    override fun processSlide(slide : Slide) {
        logger.info("Creating slide for ${slide}")
        val s = TitleAndContent(deck, slide.title)

        super.processSlide(slide)
    }

    override fun heading(head : Heading) {
    }

    override fun startBulletList(blist: BulletList) {
    }
    override fun startBulletListItem(blistitem: BulletListItem) {
    }
    override fun endBulletListItem(blistitem: BulletListItem) {
    }
    override fun endBulletList(blist: BulletList) {
    }

    override fun startOrderedListItem(olistitem: OrderedListItem) {
    }
    override fun endOrderedListItem(olistitem: OrderedListItem) {
    }

    override fun startEmphasis(em: Emphasis) {
    }
    override fun endEmphasis(em: Emphasis) {
    }

    override fun startStrongEmphasis(em: StrongEmphasis) {
    }
    override fun endStrongEmphasis(em: StrongEmphasis) {
    }

    override fun startCode(code : Code) {
    }
    override fun endCode(code: Code) {
    }

    override fun text(text : Text) {
    }
}
