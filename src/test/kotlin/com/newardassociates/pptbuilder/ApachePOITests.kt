package com.newardassociates.pptbuilder

import org.apache.poi.sl.usermodel.LineDecoration
import org.apache.poi.sl.usermodel.StrokeStyle
import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.xslf.usermodel.XSLFPlaceholderDetails
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import java.awt.Color
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.Test

class ApachePOITests {
    init {
        if (!File("./build/test-results/test/pptx/").exists()) {
            File("./build/test-results/test/pptx/").mkdirs()
        }
    }

    @Test fun generalTest() {
        // Does the test PPTX exist already?
        val apachePOITestFile = "./build/test-results/test/pptx/apachePOITest.pptx"
        val ppt =
                if (File(apachePOITestFile).exists()) { XMLSlideShow(FileInputStream(apachePOITestFile)) }
                else { XMLSlideShow() }
        val slide = ppt.createSlide(ppt.findLayout("Title and Content"))
        for (pl in slide.placeholders) {
            println("Placeholder: ${pl}")
        }

        val content = slide.placeholders[1]
        content.clearText()
        content.text = "apachePOITest"

        val para = content.addNewTextParagraph()
        val run = para.addNewTextRun()
        run.fontSize = 24.0
        run.isBold = true
        run.setText("First run")

        val run2 = para.addNewTextRun()
        run2.fontFamily = "Courier New"
        run2.setText("Second run")

        val run3 = para.addNewTextRun()
        run3.fontFamily = "Consolas"
        run3.fontSize = 20.0
        run3.isItalic = true
        run3.setText("Third run");

        val shape = slide.createTextBox()
        val p = shape.addNewTextParagraph()
        val r1 = p.addNewTextRun()
        r1.fontSize = 24.0
        r1.isItalic = true
        r1.setFontColor(Color.BLUE)
        r1.setText("apachePOITest");

        FileOutputStream(apachePOITestFile).use { out -> ppt.write(out) }
    }

    private val outPath = "build/test-results/test/pptx/apachePOITest-"

    @Test fun textboxCreation() {
        XMLSlideShow().use { ppt ->
            val slide = ppt.createSlide()
            val titleShape: XSLFTextShape = slide.createTextBox()
            titleShape.text = "This is a slide title"
            titleShape.anchor = Rectangle(50, 50, 400, 100)
            FileOutputStream(outPath + "textboxCreation.pptx").use { out -> ppt.write(out) }
        }
    }

    @Test fun iteration() {
        // Does the test PPTX exist already?
        val apachePOITestFile = "./build/test-results/test/apachePOITest.pptx"
        val ppt =
                if (File(apachePOITestFile).exists()) { XMLSlideShow(FileInputStream(apachePOITestFile)) }
                else { XMLSlideShow() }

        for (master in ppt.slideMasters) {
            println("Iterating through layouts in master ${master}")
            for (layout in master.slideLayouts) {
                println("Discovering layout ${layout.name}")
                for (ph in layout.placeholders) {
                    println("\t${layout.name} has placeholder ${ph.shapeName}")
                    if (ph.shapeName.startsWith("Footer")) {
                        ph.setText("Copyright (c) 2020 Ted Neward")
                    }
                }
            }
            for (ph in master.shapes) {
                if (ph.shapeName.startsWith("Footer")) {
                    (ph as XSLFTextShape).setText("Copyright (c) 2020 Ted Neward")
                }
            }
        }

        FileOutputStream(outPath + "iteration.pptx").use { out -> ppt.write(out) }
    }

    @Test fun paragraphExploration() {
        val ppt = XMLSlideShow()

        val slide = ppt.createSlide(ppt.findLayout("Title and Content"))

        val shape = slide.createTextBox()
        shape.fillColor = Color.YELLOW
        shape.anchor = Rectangle2D.Float(300.0f, 100.0f, 300.0f, 50.0f)
        val p = shape.addNewTextParagraph()
        val r1 = p.addNewTextRun()
        r1.setText("The")
        r1.setFontColor(Color.blue)
        r1.fontSize = 24.0
        val r2 = p.addNewTextRun()
        r2.setText(" quick")
        r2.setFontColor(Color.red)
        r2.isBold = true
        val r3 = p.addNewTextRun()
        r3.setText(" brown")
        r3.fontSize = 12.0
        r3.isItalic = true
        r3.isStrikethrough = true
        val r4 = p.addNewTextRun()
        r4.setText(" fox")
        r4.isUnderlined = true

        FileOutputStream(outPath + "paragraphExploration.pptx").use { out -> ppt.write(out) }
    }

    @Test fun codeSlideExploration() {
        val ppt = XMLSlideShow()

        val newSlide = ppt.createSlide(ppt.findLayout("Title and Content"))
        newSlide.placeholders[0].text = "Code slide"
        val body = newSlide.placeholders[1]
        val dimensions = body.anchor

        val newShape = newSlide.createTextBox()
        newShape.anchor = Rectangle2D.Double(dimensions.x, dimensions.y,
                dimensions.width, dimensions.height / 2)
        newSlide.removeShape(newSlide.placeholders[1])
        val paragraph = newShape.addNewTextParagraph()
        paragraph.setBulletStyle() // omit bullets
        paragraph.fontAlign = TextParagraph.FontAlign.TOP
        val run = paragraph.addNewTextRun()
        run.fontFamily = "Consolas"
        run.fontSize = 14.0
        run.setFontColor(Color.WHITE)
        newShape.fillColor = Color.BLACK
        run.setText("console.log('Hello world')")

        FileOutputStream(outPath + "codeSlideExploration.pptx").use { out -> ppt.write(out) }
    }

    @Test
    fun textRunAutofitTest() {
        val ppt = XMLSlideShow()

        val slide1 = ppt.createSlide()
        val shape1 = slide1.createTextBox()
        println("shape1.paragraphs = ${shape1.textParagraphs.size}")
        //shape1.textParagraphs.removeAt(0) // maybe the extra paragraph is throwing things off?
        val anchor = Rectangle(170, 100, 300, 100)
        shape1.anchor = anchor

        val p1 = shape1.addNewTextParagraph()
        println("shape1.paragraphs = ${shape1.textParagraphs.size}")
        val r1 = p1.addNewTextRun()
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
        println("shape1.anchor = ${shape1.anchor} ")

        shape1.textAutofit = org.apache.poi.sl.usermodel.TextShape.TextAutofit.NORMAL
        val resizedRect = shape1.resizeToFitText()
        println("resizedRect = $resizedRect")
        shape1.anchor = resizedRect
        println("shape1.anchor = ${shape1.anchor}")

        FileOutputStream(outPath + "Bug55391.pptx").use { out -> ppt.write(out) }
    }

    @Test fun createCodeBlocks() {
        val ppt = XMLSlideShow()

        val newSlide = ppt.createSlide(ppt.findLayout("Title Only"))
        newSlide.placeholders[0].text = "Code slide"

        val currentAnchor = Rectangle2D.Double(newSlide.placeholders[0].anchor.x,
                newSlide.placeholders[0].anchor.y,
                newSlide.placeholders[0].anchor.width,
                newSlide.placeholders[0].anchor.height)

        currentAnchor.y += currentAnchor.height + 10.0
        println("currentAnchor = $currentAnchor")

        // Add <text> block
        ;{
            val newShape = newSlide.createTextBox()
            newShape.anchor = Rectangle2D.Double(currentAnchor.x, currentAnchor.y, currentAnchor.width, 0.0)
            newShape.clearText()

            val paragraph = newShape.addNewTextParagraph()
            paragraph.fontAlign = TextParagraph.FontAlign.TOP
            val run = paragraph.addNewTextRun()
            run.setText("Hello world")
            newShape.resizeToFitText()

            currentAnchor.y += newShape.anchor.height
            println("currentAnchor = $currentAnchor")
        }()

        // Add <code> block
        ;{
            val newShape = newSlide.createTextBox()
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
            run.setText("console.log('Hello world')")
            newShape.resizeToFitText()

            currentAnchor.y += newShape.anchor.height
            println("currentAnchor = $currentAnchor")
        }()


        FileOutputStream(outPath + "createCodeBlocks.pptx").use { out -> ppt.write(out) }
    }
}