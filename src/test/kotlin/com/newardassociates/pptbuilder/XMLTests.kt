package com.newardassociates.pptbuilder

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.test.Test
import kotlin.test.assertTrue

class XMLTests {
    @Test fun xmlIncludeTest() {
        // Important note about XInclude: "The namespace for XInclude
        // was changed back to http://www.w3.org/2001/XInclude in the
        // Candidate Recommendation (April 2004). The
        // http://www.w3.org/2003/XInclude namespace is no longer
        // recognized." --https://xerces.apache.org/xerces2-j/faq-xinclude.html

        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isXIncludeAware = true
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(File("./src/test/resources/xmltest.xml"))

        assertTrue(doc.documentElement.getElementsByTagName("included").length > 0)

        // Try it with XPath
        val xpath = XPathFactory.newInstance().newXPath()
        val nodeList = xpath.compile("/presentation/body/included").evaluate(doc, XPathConstants.NODESET)
        assertTrue((nodeList as NodeList).length > 0)

        // /presentation/body is from the original document
        val bodyNodeList = xpath.compile("/presentation/body").evaluate(doc, XPathConstants.NODE) as Node
        println(bodyNodeList.baseURI)
        // /presentation/body/included is from the included document
        val includedNodeList = xpath.compile("/presentation/body/included").evaluate(doc, XPathConstants.NODE) as Node
        println(includedNodeList.baseURI)
    }
}