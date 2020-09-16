package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.XSLFTextParagraph
import org.apache.poi.xslf.usermodel.XSLFTextShape

/*
Discovering layout Title and Content
	Title and Content has placeholder Title 1
	Title and Content has placeholder Content Placeholder 2
	Title and Content has placeholder Date Placeholder 3
	Title and Content has placeholder Footer Placeholder 4
	Title and Content has placeholder Slide Number Placeholder 5
 */
class TitleAndContentSlide(deck: Deck, titleText: String)
    : Slide(deck, deck.titleContentLayout) {
    lateinit var title: XSLFTextShape
    lateinit var content: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.text = titleText
            }
            if (ph.shapeName.contains("Content")) {
                content = ph
            }
        }
        clearContent()
    }

    fun clearContent() {
        content.clearText()
    }

    fun header(text: String) {
        val para = content.addNewTextParagraph()
        para.isBullet = false
        para.indentLevel = 0
        para.setBulletStyle() // no style == no bullet character assumed

        val run = para.addNewTextRun()
        run.isBold = true
        run.setText(text)
    }

    var bulletIndex = -1 // 0 is an acceptable level of bullets
    fun newList() {
        bulletIndex += 1
    }

    fun listItem(text: String) {
        val para = content.addNewTextParagraph()
        para.isBullet = true
        para.indentLevel = bulletIndex
        val run = para.addNewTextRun()
        run.setText(text)
    }
    fun listItem() : XSLFTextParagraph {
        val para = content.addNewTextParagraph()
        para.isBullet = true
        para.indentLevel = bulletIndex
        return para
    }

    fun endList() {
        if (bulletIndex == -1)
            throw Exception("Bullet list nesting unbalanced! Too many ends against begins")
        bulletIndex -= 1
    }
}
