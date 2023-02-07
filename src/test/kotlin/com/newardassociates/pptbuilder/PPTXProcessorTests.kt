package com.newardassociates.pptbuilder

import com.newardassociates.pptbuilder.pptx.PPTXProcessor
import java.io.File
import java.util.*
import kotlin.test.Test

class PPTXProcessorTests {
    private val outPath = "build/test-results/test/pptx/"
    init {
        if (!File(outPath).exists()) {
            File(outPath).mkdirs()
        }
    }

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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOnly")).process(Parser(Properties()).parse(xmlmd))
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleSection")).process(Parser(Properties()).parse(xmlmd))
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOneContent")).process(Parser(Properties()).parse(xmlmd))
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleMarkedupTextContent")).process(Parser(Properties()).parse(xmlmd))
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleAndParagraph")).process(Parser(Properties()).parse(xmlmd))
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxTitleOrderedListContent"))
                .process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxLegacyCode() {
        val xmlmd = """
<presentation xmlns:xi="http://www.w3.org/2001/XInclude">
    <head>
        <title>Busy Developer's Guide to|pptxLegacyCode</title>
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
    <slide title="Another code slide">
        <text>Caption</text>
        <code language="js">function() {
    console.out("This is a really long code section")
    console.out("Designed to see if the autoshape stuff will work")
    console.out("Because if it doesn't I'm not sure what to do")
    console.out("So this really needs to work")
    console.out("Don't let me down Apache POI")
}
</code>
    </slide>
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxLegacyCode"))
                .process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxLegacyCodeNoTitle() {
        val xmlmd = """
<presentation xmlns:xi="http://www.w3.org/2001/XInclude">
    <head>
        <title>Busy Developer's Guide to|pptxLegacyCode</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
    <slide>
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

        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxLegacyCodeNoTitle"))
                .process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxCodeBlockTest() {
        val xmlmd = """
<presentation xmlns:xi="http://www.w3.org/2001/XInclude">
    <head>
        <title>Busy Developer's Guide to|pptxLegacyCode</title>
        <abstract>None</abstract>
        <author><name>Ted Neward</name></author>
        <audience>For any intermediate Java (2 or more years) audience</audience>
        <category>Testing</category>
        <category>Presentation</category>
    </head>
    
    <section title="Section Slide" subtitle="Section subtitle" />
<slide>
* Maven ...
    ```
    <![CDATA[<dependency>
      <groupId>org.python</groupId>
      <artifactId>jython-slim</artifactId>
      <version>2.7.2</version>
    </dependency>]]>
    ```
</slide>

<slide>
* Maven ...
    ```<![CDATA[
    <dependency>
      <groupId>org.python</groupId>
      <artifactId>jython-slim</artifactId>
      <version>2.7.2</version>
    </dependency>]]>
    ```
* ... or Gradle dependency
    ```
    implementation 'org.python:jython-slim:2.7.2'
    implementation 'org.python:jython-slim:2.7.2'
    ```
* ... because why not
</slide>
</presentation>
""".trimIndent()

        PPTXProcessor(Processor.Options(outputFilename = outPath + ::pptxCodeBlockTest.name))
                .process(Parser(Properties()).parse(xmlmd))
    }

    @Test
    fun pptxLegacyXIncludedCode() {
        PPTXProcessor(Processor.Options(outputFilename = outPath + "pptxLegacyXIncludedCode"))
                .process(Parser(Properties()).parse(File("src/test/resources/legacyXIncludedCode.xmlmd")))
    }
}
