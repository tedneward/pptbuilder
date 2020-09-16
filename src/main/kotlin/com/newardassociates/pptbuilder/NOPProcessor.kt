package com.newardassociates.pptbuilder

// --------------------------------------------------------
// Literally, do nothing--this is just to verify the input
class NOPProcessor(options : Options) : Processor(options) {
    override fun processPresentationNode(presentation : Presentation) { }
    override fun processSection(section : Section) { }
    override fun processSlide(slide : Slide) { }
}
