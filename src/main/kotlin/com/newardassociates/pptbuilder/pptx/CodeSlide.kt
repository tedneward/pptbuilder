package com.newardassociates.pptbuilder.pptx

import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.sl.usermodel.TextShape
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.util.logging.Logger

class CodeSlide(deck: Deck, titleText: String) : TitleOnlySlide(deck, titleText) {
    private val logger = Logger.getLogger(CodeSlide::class.java.canonicalName)

    val titleAnchor = title.anchor
    val currentAnchor = Rectangle2D.Double(titleAnchor.x, titleAnchor.y + titleAnchor.height + 15.0, titleAnchor.width, 0.0)

    fun addTextBlock(text: String) {
        logger.fine("Adding text block; text='$text'")

        val newShape = slide.createTextBox()
        newShape.anchor = Rectangle2D.Double(currentAnchor.x, currentAnchor.y, currentAnchor.width, 0.0)
        newShape.clearText()

        val paragraph = newShape.addNewTextParagraph()
        paragraph.fontAlign = TextParagraph.FontAlign.TOP
        val run = paragraph.addNewTextRun()
        run.setText(text)
        newShape.resizeToFitText()

        currentAnchor.y += newShape.anchor.height
        logger.fine("currentAnchor is now ${currentAnchor}")
    }

    fun addCodeBlock(code: String) {
        logger.fine("Adding code block; text='$code'")

        val newShape = slide.createTextBox()

        val numCRs = code.split(" \n\t").size
        logger.fine("codeblock has $numCRs line breaks")

        newShape.anchor = Rectangle2D.Double(currentAnchor.x, currentAnchor.y, currentAnchor.width, 0.0)
        newShape.clearText()

        val paragraph = newShape.addNewTextParagraph()
        paragraph.setBulletStyle() // omit bullets
        paragraph.fontAlign = TextParagraph.FontAlign.TOP

        val run = paragraph.addNewTextRun()
        run.fontFamily = "Consolas"
        run.fontSize = 14.0
        run.isBold = false
        run.setFontColor(Color.WHITE)
        newShape.fillColor = Color.BLACK
        run.setText(code)
        newShape.textAutofit = TextShape.TextAutofit.SHAPE
        newShape.resizeToFitText()

        currentAnchor.y += newShape.anchor.height + 5.0
        logger.fine("currentAnchor is now ${currentAnchor}")
    }
}