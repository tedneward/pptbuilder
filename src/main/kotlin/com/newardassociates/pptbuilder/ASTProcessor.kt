package com.newardassociates.pptbuilder

class ASTProcessor(options : Options) : Processor(options) {
    var contents : String = ""

    override fun processPresentationNode(presentation : Presentation) {
        contents += "(Presentation: title:${presentation.title})"
    }
    override fun processSection(section : Section) {
        contents += "(Section: $section.title)"
    }
    override fun processSlide(slide : Slide) {
        contents += "(Slide: $slide.title)"
    }

    override fun toString(): String {
        return "($contents)"
    }
}
