package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler

abstract class Processor(val options : Options) {
    data class Options(
        val outputFilename : String = "",
        var templateFile : String = "",
        var baseDirectory : String = "",
        val noTitleSlide : Boolean = false,
        val noNotesSlides : Boolean = false
    )

    open fun process(presentation : Presentation) {
        processPresentationNode(presentation)
        for (slide in presentation.slides) {
            when (slide) {
                is Slide -> processSlide(slide)
                is Section -> processSection(slide)
            }
        }
    }

    abstract fun processPresentationNode(presentation : Presentation)
    abstract fun processSection(section : Section)
    abstract fun processSlide(slide : Slide)
}
