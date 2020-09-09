package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.*

/*
Stack implementation on top of MutableList<T>
 */
fun <T> MutableList<T>.push(item: T) = this.add(this.count(), item)
fun <T> MutableList<T>.pop(): T? = if (this.count() > 0) this.removeAt(this.count() - 1) else null
fun <T> MutableList<T>.peek(): T? = if (this.count() > 0) this[this.count() - 1] else null
fun <T> MutableList<T>.hasMore() = this.count() > 0
typealias Stack<T> = MutableList<T>


class Deck(val ppt: XMLSlideShow) {
    constructor() : this(XMLSlideShow()) {}

    lateinit var titleLayout: XSLFSlideLayout
    lateinit var sectionHeaderLayout: XSLFSlideLayout
    lateinit var titleContentLayout: XSLFSlideLayout
    lateinit var titleOnlyLayout: XSLFSlideLayout
    lateinit var blankLayout: XSLFSlideLayout

    init {
        for (master in ppt.slideMasters) {
            for (layout in master.slideLayouts) {
                when (layout.type) {
                    SlideLayout.TITLE -> titleLayout = layout
                    SlideLayout.SECTION_HEADER -> sectionHeaderLayout = layout
                    SlideLayout.TITLE_AND_CONTENT -> titleContentLayout = layout
                    SlideLayout.TITLE_ONLY -> titleOnlyLayout = layout
                    SlideLayout.BLANK -> blankLayout = layout
                }
            }
        }
    }

    val properties = ppt.properties

    var title: String
        get() {
            return properties.coreProperties.title
        }
        set(value) {
            properties.coreProperties.title = value; properties.commit()
        }

    var subject: String
        get() {
            return properties.coreProperties.subject
        }
        set(value) {
            properties.coreProperties.setSubjectProperty(value); properties.commit()
        }

    var author: String
        get() {
            return properties.coreProperties.creator
        }
        set(value) {
            properties.coreProperties.creator = value; properties.commit()
        }

    var affiliation: String
        get() {
            return properties.extendedProperties.company
        }
        set(value) {
            properties.extendedProperties.company = value; properties.commit()
        }

    var manager: String
        get() {
            return properties.extendedProperties.manager
        }
        set(value) {
            properties.extendedProperties.manager = value; properties.commit()
        }

    var description: String
        get() {
            return properties.coreProperties.description
        }
        set(value) {
            properties.coreProperties.description = value; properties.commit()
        }

    var category: String
        get() {
            return properties.coreProperties.category
        }
        set(value) {
            properties.coreProperties.category = value; properties.commit()
        }

    var keywords: String
        get() {
            return properties.coreProperties.keywords
        }
        set(value) {
            properties.coreProperties.keywords = value; properties.commit()
        }
}

abstract class Slide(val deck: Deck, val layout: XSLFSlideLayout) {
    val slide = deck.ppt.createSlide(layout)
    val dimensions = deck.ppt.pageSize

    override fun toString(): String {
        return "${layout} - ${dimensions}"
    }

    /*
    TODO: Need to fix the dimensions on a Notes Slide when it's saved.
     */
    var notesBox: XSLFTextShape? = null
    var notes: String
        get() {
            return if (notesBox == null) notesBox!!.text else ""
        }
        set(value) {
            if (notesBox == null) {
                val notesSlide = deck.ppt.getNotesSlide(slide)
                notesBox = notesSlide.placeholders.filter { ph -> ph.shapeName.contains("Notes Placeholder") }[0]
                notesBox!!.clearText()
            }
            notesBox!!.text += value
        }
}

/*
Discovering layout Title Slide
	Title Slide has placeholder Title 1
	Title Slide has placeholder Subtitle 2
	Title Slide has placeholder Date Placeholder 3
	Title Slide has placeholder Footer Placeholder 4
	Title Slide has placeholder Slide Number Placeholder 5
 */
class Title(deck: Deck, titleText: String, subtitleText: String = "")
    : Slide(deck, deck.titleLayout) {
    lateinit var title: XSLFTextShape
    lateinit var subtitle: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.clearText()
                title.text = titleText
            }
            if (ph.shapeName.contains("Subtitle")) {
                subtitle = ph
                subtitle.clearText()
                subtitle.text = subtitleText
            }
        }
    }
}

/*
Discovering layout Title Only
	Title Only has placeholder Title 1
	Title Only has placeholder Date Placeholder 2
	Title Only has placeholder Footer Placeholder 3
	Title Only has placeholder Slide Number Placeholder 4
 */
class TitleOnly(deck: Deck, titleText: String)
    : Slide(deck, deck.titleOnlyLayout) {
    lateinit var title: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.text = titleText
            }
        }
    }
}

/*
Discovering layout Title and Content
	Title and Content has placeholder Title 1
	Title and Content has placeholder Content Placeholder 2
	Title and Content has placeholder Date Placeholder 3
	Title and Content has placeholder Footer Placeholder 4
	Title and Content has placeholder Slide Number Placeholder 5
 */
class TitleAndContent(deck: Deck, titleText: String)
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

    var workingBulletList: Stack<XSLFTextParagraph> = mutableListOf()
    var bulletIndex = -1 // 0 is an acceptable level of bullets
    fun newList() {
        bulletIndex += 1
    }

    fun newBulletListItem(): XSLFTextParagraph {
        val para = content.addNewTextParagraph()
        para.isBullet = true
        para.indentLevel = bulletIndex
        workingBulletList.push(para)
        return workingBulletList.peek()!!
    }

    fun newUnbulletedListItem(): XSLFTextParagraph {
        val para = newBulletListItem()
        para.isBullet = false
        return para
    }

    fun listItem(text: String) {
        val para = content.addNewTextParagraph()
        para.isBullet = true
        para.indentLevel = bulletIndex
        val run = para.addNewTextRun()
        run.setText(text)
    }

    fun endList() {
        workingBulletList.pop()
    }

    fun endBulletList() {
        if (bulletIndex == -1)
            throw Exception("Bullet list nesting unbalanced! Too many ends against begins")
        bulletIndex -= 1
    }
}

/*
Discovering layout Section Header
	Section Header has placeholder Title 1
	Section Header has placeholder Text Placeholder 2
	Section Header has placeholder Date Placeholder 3
	Section Header has placeholder Footer Placeholder 4
	Section Header has placeholder Slide Number Placeholder 5
 */
class SectionHeader(deck: Deck, titleText: String, subtitleText: String)
    : Slide(deck, deck.sectionHeaderLayout) {
    lateinit var title: XSLFTextShape
    lateinit var text: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.text = titleText
            }
            if (ph.shapeName.contains("Text")) {
                text = ph
                text.text = subtitleText
            }
        }
    }
}

/*
Discovering layout Blank
	Blank has placeholder Date Placeholder 1
	Blank has placeholder Footer Placeholder 2
	Blank has placeholder Slide Number Placeholder 3
 */
class Blank(deck: Deck) : Slide(deck, deck.blankLayout) {

}

class Code(deck: Deck, titleText: String) : Slide(deck, deck.titleContentLayout) {
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
    }

    fun addTextBlock(text: String) {

    }

    fun addCodeBlock(code: String) {

    }
}