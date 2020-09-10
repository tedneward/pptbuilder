package com.newardassociates.pptbuilder

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.test.util.AstCollectingVisitor
import com.vladsch.flexmark.util.data.MutableDataSet
import kotlin.test.Test

class MarkdownParsingTests {
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
        print(AstCollectingVisitor().collectAndGetAstText(mdAST))
    }

    @Test
    fun markdownNoHeader() {
        val markdown = """
This is just straight text

* This is some *emphasized* and some **heavily emphasized** text
* This is a bulleted list
  * This is the first indented list item
  * This is the second indented list item
"""
        val mdParserOptions = MutableDataSet()
        val mdParser = Parser.builder(mdParserOptions).build()
        val mdAST = mdParser.parse(markdown)
        print(AstCollectingVisitor().collectAndGetAstText(mdAST))
    }
}
