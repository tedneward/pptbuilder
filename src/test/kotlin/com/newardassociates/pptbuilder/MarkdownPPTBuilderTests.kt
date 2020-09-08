package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.Deck
import com.newardassociates.pptbuilder.pptx.TitleAndContent
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import org.apache.poi.xslf.usermodel.XSLFTextParagraph
import java.io.FileOutputStream
import kotlin.test.Test

class MarkdownPPTBuilderTests {
    private val outPath = "build/test-results/test/"
    @Test
    fun markdownAST() {
        val markdown = """
# This is markdown header

* This is some *emphasized* and some **heavily emphasized** text
* This is a bulleted list
  * This is the first indented list item
  * This is the second indented list item
"""
        val mdParserOptions = MutableDataSet()
        val mdParser = Parser.builder(mdParserOptions).build()
        val mdAST = mdParser.parse(markdown)

        val deck = Deck()
        val tc = TitleAndContent(deck, "Markdown Slide")
        var para: XSLFTextParagraph? = null

        fun process(node: Node) {
            when (node) {
                is Heading -> {
                    tc.header(node.childChars.unescape())
                    return // Skip processing the rest of the header for now
                }
                is BulletList -> {
                    tc.newList()
                }
                is BulletListItem -> {
                    para = tc.newBulletListItem()
                }
                is Paragraph -> {
                }
                is Code -> {
                    val run = para!!.addNewTextRun()
                    run.fontFamily = "Courier New"
                    run.setText(node.childChars.unescape())
                    return
                }
                is StrongEmphasis -> {
                    val run = para!!.addNewTextRun()
                    run!!.isBold = true
                    run.setText(node.childChars.unescape())
                    return
                }
                is Emphasis -> {
                    val run = para!!.addNewTextRun()
                    run.isItalic = true
                    run.setText(node.childChars.unescape())
                    return
                }
                is Text -> {
                    val run = para!!.addNewTextRun()
                    run.setText(node.chars.unescape())
                    return
                }
                else -> {
                    println("OTHER>>>" + node)
                }
            }
            for (n in node.childIterator) {
                process(n)
            }
        }
        for (n in mdAST.childIterator) {
            process(n)
        }

        deck.ppt.write(FileOutputStream(outPath + "markdownPPTBasics.pptx"))
    }

}
