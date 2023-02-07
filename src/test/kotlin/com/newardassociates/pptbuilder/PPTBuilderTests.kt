package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.*
import org.apache.poi.xssf.usermodel.TextAutofit
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
    fun rawXSLFTextRunTest() {
        val ppt = org.apache.poi.xslf.usermodel.XMLSlideShow();

        val slide1 = ppt.createSlide();
        val shape1 = slide1.createTextBox();
        val anchor = java.awt.Rectangle(170, 100, 300, 100);
        shape1.setAnchor(anchor);

        val p1 = shape1.addNewTextParagraph();
        val r1 = p1.addNewTextRun();
        r1.setText(
"""The Apache POI Project's mission is to create and maintain
Java APIs for manipulating various file formats based upon the Office Open
XML standards (OOXML) and Microsoft's OLE 2 Compound Document format
(OLE2). In short, you can read and write MS Excel files using Java. In
addition, you can read and write MS Word and MS PowerPoint files using
Java. Apache POI is your Java Excel solution (for Excel 97-2008). We have a
complete API for porting other OOXML and OLE2 formats and welcome others to
participate.OLE2 files include most Microsoft Office files such as XLS,
DOC, and PPT as well as MFC serialization API based file formats. Office
OpenXML Format is the new standards based XML file format found in
Microsoft Office 2007 and 2008. This includes XLSX, DOCX and PPTX.
Microsoft opened the specifications to this format in October 2007. We
would welcome contributions.""");

        shape1.setTextAutofit(org.apache.poi.sl.usermodel.TextShape.TextAutofit.NORMAL);

        val out = FileOutputStream(outPath + "Bug55391.pptx");
        ppt.write(out);
        out.close();
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

