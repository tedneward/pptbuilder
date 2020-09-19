package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.reveal.RevealProcessor
import java.io.File
import java.util.*
import kotlin.test.Test

class RevealProcessorTests {
    private val outPath = "build/test-results/test/reveal/"
    init {
        if (!File(outPath).exists()) {
            File(outPath).mkdirs()
        }
    }

    @Test fun revealTitleOnly() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Speaker's Guide to|revealTitleOnly</title>
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

        RevealProcessor(Processor.Options(outputFilename = outPath + "revealTitleOnly")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test fun revealTitleSection() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|revealTitleSection</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
</presentation>
""".trimIndent()
        RevealProcessor(Processor.Options(outputFilename = outPath + "revealTitleSection")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test fun revealTitleOneContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|revealTitleOneContent</title>
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
        RevealProcessor(Processor.Options(outputFilename = outPath + "revealTitleOneContent")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test fun revealTitleMarkedupTextContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|revealTitleMarkedupTextContent</title>
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
        RevealProcessor(Processor.Options(outputFilename = outPath + "revealTitleMarkedupTextContent")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun revealTitleOrderedListContent() {
        val xmlmd = """
<presentation>
    <head>
        <title>Busy Developer's Guide to|revealTitleOrderedListContent</title>
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

        RevealProcessor(Processor.Options(outputFilename = outPath + "revealTitleOrderedListContent")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun revealLegacyCode() {
        val xmlmd = """
<presentation xmlns:xi="http://www.w3.org/2001/XInclude">
    <head>
        <title>Busy Developer's Guide to|revealLegacyCode</title>
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
    <slide title="Code slide">
        <text>Text section</text>
        <code language="js">console.out("Code section")</code>
        <text>More text</text>
        <code language="js"><![CDATA[if (x < 4) {
  console.out("Hey this might actually work after all!");
}]]></code>
        <text>Imported code</text>
        <code language="js" src="src/test/resources/Content/code/console.js" />
        <text>Marked code</text>
        <code language="js" src="src/test/resources/Content/code/markedconsole.js" marker="console" />
    </slide>
</presentation>
""".trimIndent()

        RevealProcessor(Processor.Options(outputFilename = outPath + "revealLegacyCode")).process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun revealLegacyXIncludedCode() {
        RevealProcessor(Processor.Options(outputFilename = outPath + "revealLegacyXIncludedCode"))
                .process(Parser(Properties()).parse(File("src/test/resources/legacyXIncludedCode.xmlmd")))
    }

}