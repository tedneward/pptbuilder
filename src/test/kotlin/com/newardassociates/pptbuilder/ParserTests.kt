package com.newardassociates.pptbuilder

import org.junit.Test
import kotlin.test.assertEquals

class ParserTests {
    val parser = Parser()

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
}