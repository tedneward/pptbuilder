package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.*

class HTMLProcessor(options: Options) : Processor(options) {
    var tabCount = 0
    fun tabs() : String {
        var ret = ""
        for (i in 1..tabCount)
            ret += "_"
        return ret
    }

    override fun processPresentationNode(presentation: Presentation) {
    }

    override fun processSection(section: Section) {
    }

    override fun processSlide(slide: Slide) {
        super.processSlide(slide)
    }

    override fun heading(head: Heading) { println("!!! " + head.text) }

    override fun text(text: Text) { print(text.chars.unescape()) }

    override fun endParagraph(para: Paragraph) { println() }

    override fun startEmphasis(em: Emphasis) { print("^") }
    override fun endEmphasis(em: Emphasis) { print("^") }
    override fun startStrongEmphasis(em: StrongEmphasis) { print("%") }
    override fun endStrongEmphasis(em: StrongEmphasis) { print("%") }
    override fun startCode(code : Code) { print("\\") }
    override fun endCode(code : Code) { print("/") }

    override fun startBulletList(blist: BulletList) { tabCount += 2 }
    override fun startBulletListItem(blistitem: BulletListItem) { print(tabs() + ":") }
    override fun endBulletListItem(blistitem: BulletListItem) { println() }
    override fun endBulletList(blist: BulletList) { tabCount -= 2 }
}
