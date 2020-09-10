package com.newardassociates.pptbuilder

import java.util.*
import kotlin.test.Test

class PPTXProcessorTests {
    private val outPath = "build/test-results/test/"

    @Test
    fun pptxTitleOnly() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Speaker's Guide to|PPTB Testing</title>
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOnly.pptx")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxTitleSection() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|pptxTitleSection</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleSection.pptx")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxTitleOneContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|pptxTitleOneContent</title>
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
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOneContent.pptx")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxTitleMarkedupTextContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|pptxTitleMarkedupTextContent</title>
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
      * This is *important*
      * This is **really important**
      * But this is `code` that's not so important
    * This point is pointless
    </slide>
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleMarkedupTextContent.pptx")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxTitleAndParagraph() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|pptxTitleAndParagraph</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
    <slide title="Content slide">
    No slide header, per se
    * This is an important point
    * This is another important point
      * This is *important*
      * This is **really important**
      * But this is `code` that's not so important
    * This point is pointless
    </slide>
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleAndParagraph.pptx")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxTitleOrderedListContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|pptxTitleOrderedListContent</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
    <slide title="Content slide">
    # Our slide header
    
    1. This is an important point
    2. This is another important point
        1. Because of this
        2. And because of this
    3. But this point is pointless
    </slide>
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOrderedListContent.pptx")).process(Parser(Properties()).parse(xmlmd))
    }
}
