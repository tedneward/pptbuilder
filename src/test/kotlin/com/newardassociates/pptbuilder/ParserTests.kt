package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.Text
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTests {
    val parser = Parser(Properties())

    @Test
    fun presentationNodeOnly() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
            </presentation>
        """.trimIndent())

        assertEquals("Title!", preso.title)
        assertEquals("Abstract!", preso.abstract)
        assertEquals("Audience!", preso.audience)
        assertEquals(0, preso.slides.size)
    }

    @Test
    fun headerAndOneSlide() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                
                <slide title="Slide 1">
                This is a slide
                </slide>
            </presentation>
        """.trimIndent())

        assertEquals("Title!", preso.title)
        assertEquals("Abstract!", preso.abstract)
        assertEquals("Audience!", preso.audience)
        assertEquals(1, preso.slides.size)
    }

    @Test
    fun headerSectionAndTwoSlides() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                
                <section title="Test" subtitle="Gotta love tests..." />
                <slide title="Slide 1">
                This is a slide
                </slide>
                <slide>
                Don't you love tests?
                </slide>
            </presentation>
        """.trimIndent())

        assertEquals("Title!", preso.title)
        assertEquals("Abstract!", preso.abstract)
        assertEquals("Audience!", preso.audience)
        assertEquals(3, preso.slides.size)
    }

    @Test
    fun headerSectionAndSlideset() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                
                <slideset>
                    <section title="Test" subtitle="Gotta love tests..." />
                    <slide title="Slide 1">
                    This is a slide
                    </slide>
                    <slide>
                    Don't you love tests?
                    </slide>
                </slideset>
            </presentation>
        """.trimIndent())

        assertEquals("Title!", preso.title)
        assertEquals("Abstract!", preso.abstract)
        assertEquals("Audience!", preso.audience)
        assertEquals(3, preso.slides.size)
    }

    @Test
    fun printAST() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                
                <slideset>
                    <section title="Test" subtitle="Gotta love tests..." />
                    <slide title="Slide 1">
                    This is a slide
                    </slide>
                    <slide>
                    Don't you love tests?
                    </slide>
                </slideset>
            </presentation>
        """.trimIndent())

        ASTProcessor(Processor.Options(outputFilename = "")).process(preso)
    }

    @Test
    fun trimmingTests() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                
                <slideset>
                    <section title="Test" subtitle="Gotta love tests..." />
<slide title="Unindented slide test">
This is one line.
This is a second.
</slide>
        <slide title="Indented twice line">
        This is *Sparta*!
        
        This is the second line but should be a different paragraph.
        </slide>
                    <slide title="Indented four times line">
                    This **is** Sparta.
                    This is the second line of the same paragraph.
                    </slide>
                    <slide title="Content indented more than the tag line">
                        # This is a header.
                        * Use the `printf` function
                        * This is **another** list item
                    </slide>
                </slideset>
            </presentation>
        """.trimIndent())

        ASTProcessor(Processor.Options(outputFilename = "")).process(preso)

        assertTrue(preso.slides[0] is Section)
        val testSection = (preso.slides[0] as Section)
        assertEquals("Test", testSection.title)

        val unindentedSlideTest = (preso.slides[1] as Slide)
        assertEquals("Document{}", unindentedSlideTest.markdownBody.toString())
        assertEquals(1, unindentedSlideTest.markdownBody.children.count() )
        val unindentedSlideTestParagraphNode = unindentedSlideTest.markdownBody.firstChild as Paragraph
        assertEquals(3, unindentedSlideTestParagraphNode.children.count())
        val ustFirstLine = unindentedSlideTestParagraphNode.firstChild as Text
        assertEquals("This is one line.", ustFirstLine.chars.unescape())
        val ustSecondLine = unindentedSlideTestParagraphNode.children.drop(2).first() as Text
        assertEquals("This is a second.", ustSecondLine.chars.unescape())
    }

    @Test
    fun slidesWithBulletLists() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                <slide title="Content indented more than the tag line">
                    # This is a header.
                    * First list item
                      * First list first nested sublist item
                      * First list second nested sublist item
                    * Second list item
                      * Second list first nested sublist item
                      * Second list second nested sublist item
                </slide>
            </presentation>
        """.trimIndent())

        val ast = ASTProcessor(Processor.Options(outputFilename = ""))
        ast.process(preso)
    }
    @Test
    fun codeSlideURLTest() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                <slide title="Code from URL test">
                    <code src="slidesamples/legacy/Testing/App.java" />
                </slide>
                <slide title="Code from URL test">
                    <code src="https://raw.githubusercontent.com/tedneward/pptbuilder/master/slidesamples/legacy/Testing/App.java" />
                </slide>
            </presentation>
        """.trimIndent())
    }
    @Test
    fun codeblockTest() {
        val preso = parser.parse(""" 
            <presentation>
                <head>
                    <title>Title!</title>
                    <abstract>Abstract!</abstract>
                    <audience>Audience!</audience>
                </head>
                <slide title="Codeblock test">
                # Header

                ```
                This is a code block. Line break here
                And another linebreak here.
                And one more for fun.
                ```
                </slide>
            </presentation>
        """.trimIndent())

        val ast = ASTProcessor(Processor.Options(outputFilename = ""))
        ast.process(preso)

        assertTrue(preso.slides[0] is Slide)
        val codeblockTestSlide = (preso.slides[0] as Slide)
        val body = codeblockTestSlide.markdownBody
        val paraCount = body.children.count()
        assertEquals(2, paraCount)
        // first node should be Header

        // second node should be a CodeBlock
    }
}