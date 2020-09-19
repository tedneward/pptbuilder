package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.*
import java.io.File
import java.io.FileOutputStream
import kotlin.test.BeforeTest
import kotlin.test.Test

class PPTBuilderTests {
    private val outPath = "build/test-results/test/pptx/"
    init {
        if (!File(outPath).exists()) {
            File(outPath).mkdirs()
        }
    }

    lateinit var deck: Deck

    @BeforeTest
    fun setupDeck() {
        deck = Deck()
    }

    @Test
    fun writeTitleAndContentSlide() {
        val tc = TitleAndContentSlide(deck, "Title Slide")
        tc.content.text = "Content text"

        deck.ppt.write(FileOutputStream(outPath + "writeTitleAndContentSlide.pptx"))
    }

    @Test
    fun writeTitleOnlySlide() {
        val tc = TitleOnlySlide(deck, "Title")

        deck.ppt.write(FileOutputStream(outPath + "writeTitleOnlySlide.pptx"))
    }

    @Test
    fun writeTitleSlide() {
        val ts = TitleSlide(deck, "Title Slide Text", "Subtitle Text")

        println(ts)

        deck.ppt.write(FileOutputStream(outPath + "writeTitleSlide.pptx"))
    }

    @Test
    fun writeSectionSlide() {
        SectionHeaderSlide(deck, "Title Slide Text", "Subtitle Text")
        deck.ppt.write(FileOutputStream(outPath + "writeSectionSlide.pptx"))
    }

    @Test
    fun writeDeckMetadata() {
        deck.title = "Write Deck Metadata Test"
        deck.author = "Ted Neward"
        deck.subject = "Writing Deck Metadata in a test is a hefty subject"
        deck.description = "Test deck to see if properties are stored"
        deck.affiliation = "Neward & Associates"
        deck.keywords = "Test test test"

        deck.ppt.write(FileOutputStream(outPath + "writeDeckMetadata.pptx"))
    }

    @Test
    fun writeNotesInSlides() {
        val ts = TitleSlide(deck, "Title Slide Text", "Subtitle Text")
        ts.notes = "These are some notes to go along with the slide"

        val tc = TitleAndContentSlide(deck, "Title Slide")
        tc.content.text = "Content text"
        tc.notes = "These are some more notes to go along with the slide"

        deck.ppt.write(FileOutputStream(outPath + "writeNotesInSlides.pptx"))
    }

    @Test
    fun writeHeader() {
        val ts = TitleAndContentSlide(deck, "Title Slide Text")
        ts.header("Important stuff!")

        deck.ppt.write(FileOutputStream(outPath + "writeHeader.pptx"))
    }

    @Test
    fun writeHeaderAndOneLevelBulletList() {
        val ts = TitleAndContentSlide(deck, "Title Slide Text")
        ts.header("Important stuff!")

        ts.newList()
        ts.listItem("This is a bullet point")
        ts.listItem("This is a second bullet point")
        ts.listItem("This is a third bullet point")
        ts.endList()

        deck.ppt.write(FileOutputStream(outPath + "writeHeaderAndOneLevelBulletList.pptx"))
    }

    @Test
    fun writeHeaderAndFourLevelsBulletList() {
        val ts = TitleAndContentSlide(deck, "Title Slide Text")
        ts.header("Important stuff!")

        ts.newList()
        ts.listItem("This is a bullet point")
        ts.newList()
        ts.listItem("This is a sub-point")
        ts.newList()
        ts.listItem("This is a sub-sub-point")
        ts.newList()
        ts.listItem("This is a sub-sub-sub-point")
        ts.endList()
        ts.endList()
        ts.listItem("THis is a second sub-point")
        ts.endList()
        ts.listItem("This is a second bullet point")
        ts.listItem("This is a third bullet point")
        ts.endList()

        deck.ppt.write(FileOutputStream(outPath + "writeHeaderAndFourLevelsBulletList.pptx"))
    }

    @Test
    fun writeHeaderWithBulletRuns() {
        val ts = TitleAndContentSlide(deck, "Title Slide Text")
        ts.header("Important stuff!")

        ts.newList()
        val para = ts.listItem()
        val run1 = para.addNewTextRun()
        run1.setText("This ")
        val run2 = para.addNewTextRun()
        run2.setText("is ")
        run2.isItalic = true
        val run3 = para.addNewTextRun()
        run3.setText("emphasized ")
        run3.isItalic = true
        run3.isBold = true
        val run4 = para.addNewTextRun()
        run4.isBold = true
        run4.setText("text")

        ts.endList()

        deck.ppt.write(FileOutputStream(outPath + "writeHeaderWithBulletRuns.pptx"))
    }
}

