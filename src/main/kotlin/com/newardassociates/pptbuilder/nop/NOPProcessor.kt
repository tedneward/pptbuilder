package com.newardassociates.pptbuilder.nop

import com.newardassociates.pptbuilder.Presentation
import com.newardassociates.pptbuilder.Processor
import com.newardassociates.pptbuilder.Section
import com.newardassociates.pptbuilder.Slide

// --------------------------------------------------------
// Literally, do nothing--this is just to verify the input
class NOPProcessor(options : Options) : Processor(options) {
    override fun processPresentationNode(presentation : Presentation) { }
    override fun processSection(section : Section) { }
    override fun processSlide(slide : Slide) { }
}
