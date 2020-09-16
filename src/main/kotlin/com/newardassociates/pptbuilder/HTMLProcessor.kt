package com.newardassociates.pptbuilder

import com.vladsch.flexmark.ast.*

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
