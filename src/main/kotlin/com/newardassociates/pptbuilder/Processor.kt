package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler

abstract class Processor(val options : Options) {
    data class Options(
        val outputFilename : String,
        val noTitleSlide : Boolean = false,
        val templateFile : String? = null
    )

    open fun process(presentation : Presentation) {
        processPresentationNode(presentation)
        for (slide in presentation.slides) {
            when (slide) {
                is Slide -> processSlide(slide)
                is Section -> processSection(slide)
            }
        }
    }

    abstract fun processPresentationNode(presentation : Presentation)
    abstract fun processSection(section : Section)
    open fun processSlide(slide : Slide) {
        val visitor = NodeVisitor()

        // Other nodes to process
        // CodeBlock
        // Image
        // ImageRef
        // Link
        // BulletedListItem?
        // OrderedListItem?

        visitor.addHandler(VisitHandler(Heading::class.java, fun (head : Heading) {
            heading(head)
            //visitor.visitChildren(head)
        }))

        visitor.addHandler(VisitHandler(Paragraph::class.java, fun (para : Paragraph) {
            startParagraph(para)
            visitor.visitChildren(para)
            endParagraph(para)
        }))

        visitor.addHandler(VisitHandler(SoftLineBreak::class.java, fun (slb : SoftLineBreak) {
            //processSoftLineBreak(slb)
            visitor.visitChildren(slb)
        }))

        visitor.addHandler(VisitHandler(Text::class.java, fun (text : Text) {
            text(text)
            visitor.visitChildren(text)
        }))
        visitor.addHandler(VisitHandler(Emphasis::class.java, fun(em : Emphasis) {
            startEmphasis(em)
            visitor.visitChildren(em)
            endEmphasis(em)
        }))
        visitor.addHandler(VisitHandler(StrongEmphasis::class.java, fun(em : StrongEmphasis) {
            startStrongEmphasis(em)
            visitor.visitChildren(em)
            endStrongEmphasis(em)
        }))
        visitor.addHandler(VisitHandler(Code::class.java, fun (code : Code) {
            startCode(code)
            visitor.visitChildren(code)
            endCode(code)
        }))

        visitor.addHandler(VisitHandler(FencedCodeBlock::class.java, fun (block : FencedCodeBlock) {
            startCodeBlock(block)
            visitor.visitChildren(block)
            endCodeBlock(block)
        }))

        visitor.addHandler(VisitHandler(BulletList::class.java, fun (blist : BulletList) {
            startBulletList(blist)
            visitor.visitChildren(blist)
            endBulletList(blist)
        }))
        visitor.addHandler(VisitHandler(BulletListItem::class.java, fun (blistitem : BulletListItem) {
            startBulletListItem(blistitem)
            visitor.visitChildren(blistitem)
            endBulletListItem(blistitem)
        }))
        visitor.addHandler(VisitHandler(OrderedList::class.java, fun (olist : OrderedList) {
            startOrderedList(olist)
            visitor.visitChildren(olist)
            endOrderedList(olist)
        }))
        visitor.addHandler(VisitHandler(OrderedListItem::class.java, fun (olistitem : OrderedListItem) {
            startOrderedListItem(olistitem)
            visitor.visitChildren(olistitem)
            endOrderedListItem(olistitem)
        }))

        visitor.visit(slide.markdownBody)
    }

    open fun heading(head : Heading) { }

    open fun startParagraph(para : Paragraph) { }
    open fun endParagraph(para : Paragraph) { }

    open fun text(text : Text) { }
    open fun startEmphasis(em : Emphasis) { }
    open fun endEmphasis(em: Emphasis) { }
    open fun startStrongEmphasis(em: StrongEmphasis) { }
    open fun endStrongEmphasis(em: StrongEmphasis) { }
    open fun startCode(code : Code) { }
    open fun endCode(code : Code) { }

    open fun startCodeBlock(block : FencedCodeBlock) { }
    open fun endCodeBlock(block: FencedCodeBlock) { }

    open fun startBulletList(blist: BulletList) { }
    open fun startBulletListItem(blistitem: BulletListItem) { }
    open fun endBulletListItem(blistitem: BulletListItem) { }
    open fun endBulletList(blist: BulletList) { }
    open fun startOrderedList(orderedList: OrderedList) { }
    open fun startOrderedListItem(olistitem: OrderedListItem) { }
    open fun endOrderedListItem(olistitem: OrderedListItem) { }
    open fun endOrderedList(orderedList: OrderedList) { }
}

// --------------------------------------------------------

class ASTProcessor(options : Options) : Processor(options) {

    var contents : String = ""

    override fun processPresentationNode(presentation : Presentation) {
        contents += "(Presentation: title:${presentation.title})"
    }
    override fun processSection(section : Section) {
        contents += "(Section: $section.title)"
    }
    override fun processSlide(slide : Slide) {
        contents += "(Slide: $slide.title)"
    }

    override fun toString(): String {
        return "($contents)"
    }
}

class TextProcessor(options: Options) : Processor(options) {
    var tabCount = 0
    fun tabs() : String {
        var ret = ""
        for (i in 1..tabCount)
            ret += "_"
        return ret
    }

    override fun processPresentationNode(presentation: Presentation) {
        println("Presentation: ${presentation.title}")
    }

    override fun processSection(section: Section) {
        println("Section: ${section.title}")
    }

    override fun processSlide(slide: Slide) {
        println("Slide: ${slide.title}")
        super.processSlide(slide)
    }

    override fun heading(head: Heading) { println("!!! " + head.text) }

    override fun text(text: Text) { print(text.chars.unescape()) }

    override fun endParagraph(para: Paragraph) { println() }

    override fun startEmphasis(em: Emphasis) { print("EM(") }
    override fun endEmphasis(em: Emphasis) { print(")") }
    override fun startStrongEmphasis(em: StrongEmphasis) { print("STEM(") }
    override fun endStrongEmphasis(em: StrongEmphasis) { print(")") }
    override fun startCode(code : Code) { print("CODE(") }
    override fun endCode(code : Code) { print(")") }

    override fun startBulletList(blist: BulletList) { tabCount += 2 }
    override fun startBulletListItem(blistitem: BulletListItem) { print(tabs() + ":") }
    override fun endBulletListItem(blistitem: BulletListItem) { println() }
    override fun endBulletList(blist: BulletList) { tabCount -= 2 }
}


/*
class PDFProcessor : Processor("pdf") {

}
*/