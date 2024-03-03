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
    val slides : List<Node>,
    val footnotes : List<Footnote> = listOf()
)

data class Section(
    val title: String,
    val subtitle: String?,
    val quote: String?,
    val attribution: String?,
    val notes : List<String>,
    val defaultSlideTitle: String = title
) : Node()

data class Slide(
    val title : String,
    val node : XMLNode,
    val markdownBody : MDDocument,
    val rawBody : String,
    val notes : List<String>
) : Node()

data class Footnote(
    val ref : String,
    val bibliography : String
)

// Different kinds of slides?
/*
data class Slide(
    val node : XMLNode,
    val notes : List<String>
) : Node()

data class SectionSlide(
    val title: String,
    val subtitle: String?,
    val quote: String?,
    val attribution: String?,
    val defaultSlideTitle: String = title
) : Slide()

// A slide that is a single image, full-screen, no title
data class ZenSlide(
    val image : URI,
    val caption : String
)

// A slide that is two-columns, both markdown text
data class TwoColSlide(
    val title : String,
    val leftMarkdown : MDDocument,
    val rightMarkdown : MDDocument    
)
 */

