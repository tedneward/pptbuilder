package com.newardassociates.pptbuilder.text

import com.newardassociates.pptbuilder.Presentation
import com.newardassociates.pptbuilder.Processor
import com.newardassociates.pptbuilder.Section
import com.newardassociates.pptbuilder.Slide

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
