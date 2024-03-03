package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.ext.attributes.AttributesExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.test.util.AstCollectingVisitor
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class MarkdownParsingTests {
    @Test fun flexmarkMarkdownParse() {
        val options = MutableDataSet()

        //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        val parser: Parser = Parser.builder(options).build()
        val document: Node = parser.parse("This is *Sparta*")

        val renderer: HtmlRenderer = HtmlRenderer.builder(options).build()
        val html: String = renderer.render(document) // "<p>This is <em>Sparta</em></p>\n"
        assertNotNull(html, "<p>This is <em>Sparta</em></p>\\n")
    }

    @Test fun basicMarkdownCodeBlock() {
        val options = MutableDataSet()

        val parser: Parser = Parser.builder(options).build()
        val input = """
            There is a method called `doSomething` and it looks like this:

            ```
            cout << "I did something!" << endl;
            ```
            
            But that's all there is.
            """.trimIndent()

        val document: Node = parser.parse(input)

        for (child in document.children) {
            println("\t" + child)
        }
        // Produces:
        /*
            Paragraph{}
            FencedCodeBlock{}
            Paragraph{}
         */
    }

    @Test fun attributedMarkdownCodeBlock1() {
        val options: DataHolder = MutableDataSet()
            .toImmutable()

        val parser: Parser = Parser.builder(options).build()
        val input = """
            Now I would like some code with a language attached: {title="Title"}
            
            ```javascript {src=./code/console.js}
            ```

            And now our slide is complete.
            """.trimIndent()

        val document: Node = parser.parse(input)

        for (child in document.children) {
            println(child)
            when (child) {
                is FencedCodeBlock -> {
                    println("\tinfo: >>${child.info}<<")
                }
            }
        }
        // Produces:
        /*

         */
    }
    @Test fun attributedMarkdownCodeBlock2() {
        val options: DataHolder = MutableDataSet()
            .toImmutable()

        val parser: Parser = Parser.builder(options).build()
        val input = """
            Now I would like some code with a language attached: {title="Title"}
            
            Let's show off some embedded code:
            ``` {language=javascript}
            console.log("This code is in the XMLMD file")
            ```

            And now our slide is complete.
            """.trimIndent()

        val document: Node = parser.parse(input)

        for (child in document.children) {
            println(child)
            when (child) {
                is FencedCodeBlock -> {
                    println("\tinfo: ${child.info}")
                }
            }
        }
        // Produces:
        /*
Paragraph{}
Paragraph{}
FencedCodeBlock{}
	info: {language=javascript}
Paragraph{}
         */
    }
    @Test fun attributedMarkdownCodeBlock3() {
        val options = MutableDataSet()

        val parser: Parser = Parser.builder(options).build()
        val input = """
            Now I would like some code with a language attached: {title="Title"}
            
            Here is some imported code from `console.js`:
            ``` {language=javascript src=Content/code/console.js}
            console.log("More code")
            ```

            And now our slide is complete.
            """.trimIndent()

        val document: Node = parser.parse(input)

        for (child in document.children) {
            println(child)
            when (child) {
                is FencedCodeBlock -> {
                    println("\tinfo: ${child.info}")
                }
            }
        }
        // Produces:
        /*
Paragraph{}
Paragraph{}
FencedCodeBlock{}
	info: {language=javascript src=Content/code/console.js}
Paragraph{}
         */
    }

    @Test fun markdownCodeBlockTest() {
        val options = MutableDataSet()

        val parser: Parser = Parser.builder(options).build()
        val input = """
            There is a method called `doSomething` and it looks like this:
            ```
            cout << "I did something!" << endl;
            ```
            
            There is another method, called `doSomethingElse` and it looks like:
            
                    cout << "I did something different" << endl;

            Now I would like some code with a language attached: {title="Title"}
            
            ```javascript{src=./code/console.js}
            ```

            Let's show off some embedded code:
            ``` {language=javascript}
            console.log("This code is in the XMLMD file")
            ```
    
            Here is some imported code from `console.js`:
            ``` {language=javascript src=Content/code/console.js}
            console.log("More code")
            ```

            And now our slide is complete.
        """.trimIndent()
        val document: Node = parser.parse(input)

        for (child in document.children) {
            println("\t" + child)
            if (child is IndentedCodeBlock) {
                println("ICBCODE: " + child.contentChars)
            }
            if (child is FencedCodeBlock) {
                println("FCBCODE: " + child.contentChars)
                println("  attributes: ${child.attributes.base}")
                println("  info: ${child.info}")
            }
        }
        println("End of document")
    }

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

    @Test
    fun markdownImage() {
        val testFileName = "Content/test.png"

        val markdown = """
            ![]($testFileName)
        """.trimIndent()
        val mdParserOptions = MutableDataSet()
        val mdParser = Parser.builder(mdParserOptions).build()
        val mdAST = mdParser.parse(markdown)
        // mdAST: [Document[Paragraph[Image[]]]]
        val img = (mdAST.children.elementAt(0).children.elementAt(0) as Image)
        assertEquals(testFileName, img.url.unescape())
        print(AstCollectingVisitor().collectAndGetAstText(mdAST))
    }
}
