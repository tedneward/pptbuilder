package com.newardassociates.pptbuilder

import com.vladsch.flexmark.util.ast.Document as MDDocument

sealed class Node(
    val tag: Any? = null
)

data class Presentation(
    val title : String,
    val abstract : String,
    val audience : String,
    val author : String,
    val affiliation : String,
    val contactInfo : Map<String, String>,
    val keywords : List<String>,
    val slides : List<Node>
)

data class Slide(
    val title : String,
    val markdownBody : MDDocument,
    val rawBody : String
) : Node()

data class Section(
    val title : String,
    val quote : String?,
    val attribution : String?,
    val defaultSlideTitle : String = title
) : Node()
