package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.slidy.SlidyProcessor
import java.io.File
import java.util.*
import kotlin.test.Test

class SlidyProcessorTests {
    private val outPath = "build/test-results/test/slidy/"
    init {
        if (!File(outPath).exists()) {
            File(outPath).mkdirs()
        }
    }

    @Test fun slidyTitleOnly() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Speaker's Guide to|slidyTitleOnly</title>
        <abstract>
            This is the abstract for a sample talk.

            This is the second paragraph for an abstract.
        </abstract>
        <author>
            <name>Ted Neward</name>
            <contact>
                <email>ted@tedneward.com</email>
                <linkedin>tedneward</linkedin>
                <blog>http://blogs.newardassociates.com</blog>
            </contact>
        </author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
</presentation>
""".trimIndent()

        SlidyProcessor(Processor.Options(outputFilename = outPath + "slidyTitleOnly.html")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test fun slideTitleSection() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|slidyTitleSection</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
</presentation>
""".trimIndent()
        SlidyProcessor(Processor.Options(outputFilename = outPath + "slidyTitleSection.html")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test fun slidyTitleOneContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|slidyTitleOneContent</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
    <slide title="Content slide">
    # Our slide header
    * This is an important point
    * This is another important point
    * This point is pointless
    </slide>
</presentation>
"""
        SlidyProcessor(Processor.Options(outputFilename = outPath + "slidyTitleOneContent.html")).process(Parser(Properties()).parse(xmlmd))
    }
}