package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.*
import org.apache.poi.xslf.usermodel.*
import java.io.FileOutputStream
import java.time.Instant
import java.util.*
import java.util.logging.Logger


/*
Stack implementation on top of MutableList<T>
 */
fun <T> MutableList<T>.push(item: T) = this.add(this.count(), item)
fun <T> MutableList<T>.pop(): T? = if(this.count() > 0) this.removeAt(this.count() - 1) else null
fun <T> MutableList<T>.peek(): T? = if(this.count() > 0) this[this.count() - 1] else null
fun <T> MutableList<T>.hasMore() = this.count() > 0
typealias Stack<T> = MutableList<T>

// Things to do:
// -- start from or associate to PPTX template
class PPTXProcessor(options : Options) : Processor(options) {
    val logger = Logger.getLogger("PPTXProcessor")

    override fun process(presentation: Presentation) {
        logger.info("Beginning PPTX processing of ${presentation.title} to ${options.outputFilename}")

        super.process(presentation)

        logger.info("Writing out file")
        val out = FileOutputStream(options.outputFilename + ".pptx")
        slideshow.write(out)
        out.close()
    }

    val slideshow : XMLSlideShow = XMLSlideShow()
    lateinit var titleLayout : XSLFSlideLayout
    lateinit var sectionHeaderLayout : XSLFSlideLayout
    lateinit var titleContentLayout : XSLFSlideLayout

    // Between-methods working state
    lateinit var workingSlide : XSLFSlide
    var bulletLevel = 0
    // Bulleted and ordered list items (for nesting)
    var listStack : Stack<XSLFTextParagraph> = mutableListOf()
    var workingParagraph : XSLFTextParagraph? = null
    var workingRun : XSLFTextRun? = null

    init {
        for (master in slideshow.slideMasters) {
            logger.fine("Iterating through layouts in master ${master}")
            for (layout in master.slideLayouts) {
                logger.fine("Discovering layout ${layout.name}")
                when (layout.type) {
                    SlideLayout.TITLE -> titleLayout = layout
                    SlideLayout.SECTION_HEADER -> sectionHeaderLayout = layout
                    SlideLayout.TITLE_AND_CONTENT -> titleContentLayout = layout
                }
            }
        }
    }

    override fun processPresentationNode(presentation: Presentation) {

        fun contactLineFromContactInfo(contactInfo: Map<String, String>) : List<String> {
            val returnList = mutableListOf<String>()

            // Arranging this intelligently is going to be a bitch and a half

            return returnList
        }

        // Create title slide
        val slide = slideshow.createSlide(titleLayout)

        val titleTextbox = slide.getPlaceholder(0)
        val titleText = presentation.title.replace("|", "\n")
        titleTextbox.setText(titleText)

        val subtitleTextbox = slide.getPlaceholder(1)
        subtitleTextbox.clearText()

        val author = "${presentation.author} | ${presentation.affiliation}"
        // The contact line really should be all hyperlinks, when you think about it
        val authorRun = subtitleTextbox.addNewTextParagraph().addNewTextRun()
        authorRun.setText(author)

        // Email
        /*
        val contact = "e: ${presentation.contactInfo["email"].toString()} | t: ${presentation.contactInfo["twitter"].toString()}\n"
        val contactPara = subtitleTextbox.addNewTextParagraph()
        val emailRun = contactPara.addNewTextRun()
        emailRun.fontSize = (authorRun.fontSize / 4) * 3
        val emailHyperlink = emailRun.createHyperlink()
        emailHyperlink.address = "mailto:" + presentation.contactInfo["email"].toString()
        emailHyperlink.label = presentation.contactInfo["email"].toString()

        val sepRun = contactPara.addNewTextRun()
        sepRun.fontSize = (authorRun.fontSize / 4) * 3
        sepRun.setText(" | ")

        val twitterRun = subtitleTextbox.addNewTextParagraph().addNewTextRun()
        twitterRun.fontSize = (authorRun.fontSize / 4) * 3
        val twitterHyperlink = twitterRun.createHyperlink()
        val twitterHandle = presentation.contactInfo["twitter"].toString().dropWhile { c -> c == 'W' }
        twitterHyperlink.address = "http://twitter.com/${twitterHandle}"
        twitterHyperlink.label = twitterHandle
         */

        // Edit deck properties
        val coreProperties = slideshow.properties.coreProperties
        //coreProperties.keywords = presentation.keywords
        coreProperties.title = presentation.title
        coreProperties.description = presentation.abstract.trimIndent()
        logger.info("Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)} ${presentation.author}")
        coreProperties.creator =
                "Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)} ${presentation.author}"

        coreProperties.keywords = "Keywords"
        coreProperties.category = "Category"
        /*
        // None of these seem to appear in PPTX properties on macOS
        coreProperties.identifier = "Identifier"
        coreProperties.contentStatus = "Content Status"
        coreProperties.revision = "Revision"
        coreProperties.setSubjectProperty("Subject")
         */

    }

    override fun processSection(section: Section) {
        val slide = slideshow.createSlide(sectionHeaderLayout)

        val title = slide.getPlaceholder(0)
        title.text = section.title
    }

    override fun processSlide(slide : Slide) {
        workingSlide = slideshow.createSlide(titleContentLayout)

        val title = workingSlide.placeholders[0]
        val titleText = slide.title
        title.text = titleText

        val body = workingSlide.placeholders[1]
        body.clearText()

        super.processSlide(slide)

        listStack.clear()
    }

    override fun heading(head : Heading) {
        workingParagraph!!.isBullet = false
        workingParagraph!!.indentLevel = 0
        workingParagraph!!.setBulletStyle() // no style == no bullet character assumed

        val run = workingParagraph!!.addNewTextRun()
        run.isBold = true
        run.setText(head.text.unescape())

        workingParagraph = workingSlide.placeholders[1].addNewTextParagraph()
    }

    override fun startBulletList(blist: BulletList) {
        bulletLevel += 1
        logger.info("Starting bullet list; bulletLevel = ${bulletLevel}")
    }
    override fun startBulletListItem(blistitem: BulletListItem) {
        logger.info("Starting bullet list item ${blistitem.chars.unescape()} at bulletLevel ${bulletLevel}")
        val content = workingSlide.placeholders[1]

        workingParagraph = content.addNewTextParagraph()
        workingParagraph!!.indentLevel = (bulletLevel - 1)

        listStack.push(workingParagraph!!)
        workingRun = workingParagraph!!.addNewTextRun()
    }
    override fun endBulletListItem(blistitem: BulletListItem) {
        workingRun = null
        workingParagraph = listStack.pop()
    }
    override fun endBulletList(blist: BulletList) {
        bulletLevel -= 1
        logger.info("End bullet list; bulletLevel = ${bulletLevel}")
    }

    override fun startOrderedListItem(olistitem: OrderedListItem) {
        logger.info("Starting ordered list item ${olistitem.chars.unescape()}")
        val content = workingSlide.placeholders[1]

        workingParagraph = content.addNewTextParagraph()
        workingParagraph!!.indentLevel = listStack.size
        listStack.push(workingParagraph!!)
    }
    override fun endOrderedListItem(olistitem: OrderedListItem) {
        listStack.pop()
    }

    override fun startEmphasis(em: Emphasis) {
        logger.info("Start Emphasis")
        workingRun = workingParagraph!!.addNewTextRun()
        workingRun!!.isItalic = true
    }
    override fun endEmphasis(em: Emphasis) {
        logger.info("End emphasis")
        workingRun = null
    }

    override fun startStrongEmphasis(em: StrongEmphasis) {
        logger.info("Start StrongEm")
        workingRun = workingParagraph!!.addNewTextRun()
        workingRun!!.isBold = true
    }
    override fun endStrongEmphasis(em: StrongEmphasis) {
        logger.info("End StrongEm")
        workingRun = null
    }

    override fun startCode(code : Code) {
        logger.info("Start Code")
        workingRun = workingParagraph!!.addNewTextRun()
        workingRun!!.fontFamily = "Courier New"
    }
    override fun endCode(code: Code) {
        logger.info("End code")
        workingRun = null
    }

    override fun text(text : Text) {
        logger.info("Text: ${text.chars.unescape()}")
        workingRun = if (workingRun == null) workingParagraph!!.addNewTextRun() else workingRun
        workingRun!!.setText(text.chars.unescape())
    }
}
