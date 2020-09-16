package com.newardassociates.pptbuilder.html

import com.newardassociates.pptbuilder.Presentation
import com.newardassociates.pptbuilder.Processor
import com.newardassociates.pptbuilder.Section
import com.newardassociates.pptbuilder.Slide

class HTMLProcessor(options: Options) : Processor(options) {
    var tabCount = 0
    fun tabs() : String {
        var ret = ""
        for (i in 1..tabCount)
            ret += "_"
        return ret
    }

    override fun processPresentationNode(presentation: Presentation) {
    }

    override fun processSection(section: Section) {
    }

    override fun processSlide(slide: Slide) {
    }
}
