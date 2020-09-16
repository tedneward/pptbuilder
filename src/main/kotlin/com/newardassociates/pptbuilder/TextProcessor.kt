package com.newardassociates.pptbuilder

class TextProcessor(options: Options) : Processor(options) {
    var tabCount = 0
    fun tabs() : String {
        var ret = ""
        for (i in 1..tabCount)
            ret += "_"
        return ret
    }

    override fun processPresentationNode(presentation: Presentation) {
        println("Presentation: ${presentation.title}")
    }

    override fun processSection(section: Section) {
        println("Section: ${section.title}")
    }

    override fun processSlide(slide: Slide) {
        println("Slide: ${slide.title}")
    }
}
