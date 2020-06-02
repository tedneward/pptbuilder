package com.newardassociates.pptbuilder

import com.vladsch.flexmark.util.ast.Node as MDNode

sealed class Node(
    val tag: Any? = null
)

data class Presentation(
    val title : String,
    val abstract : String,
    val audience : String,
    val slides : List<Node>
) : Node()

data class Slide(
    val title : String?,
    //val markdownBody : MDNode,
    val rawBody : String?
) : Node()

data class Section(
    val title : String,
    val quote : String?,
    val attribution : String?,
    val defaultSlideTitle : String = title
) : Node()

data class Import(
    val href : String  // the PPT to import
) : Node()