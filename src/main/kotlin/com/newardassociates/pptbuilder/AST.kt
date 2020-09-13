package com.newardassociates.pptbuilder

import com.vladsch.flexmark.util.ast.Document as MDDocument
import org.w3c.dom.Node as XMLNode

sealed class Node(
    val tag: Any? = null
)

data class Presentation(
    val title : String,
    val abstract : String,
    val audience : String,
    val author : String,
    val jobTitle : String,
    val affiliation : String,
    val contactInfo : Map<String, String>,
    val keywords : List<String>,
    val slides : List<Node>
)

data class Slide(
    val title : String,
    val node : XMLNode,
    val markdownBody : MDDocument,
    val rawBody : String,
    val notes : List<String>
) : Node()

data class Section(
        val title: String,
        val subtitle: String?,
        val quote: String?,
        val attribution: String?,
        val defaultSlideTitle: String = title
) : Node()

data class Import(
        val importingPPT : String
) : Node()
