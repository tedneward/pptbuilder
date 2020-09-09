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
}