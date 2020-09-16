package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.XSLFTextShape

/*
Discovering layout Section Header
	Section Header has placeholder Title 1
	Section Header has placeholder Text Placeholder 2
	Section Header has placeholder Date Placeholder 3
	Section Header has placeholder Footer Placeholder 4
	Section Header has placeholder Slide Number Placeholder 5
 */
class SectionHeaderSlide(deck: Deck, titleText: String, subtitleText: String)
    : Slide(deck, deck.sectionHeaderLayout) {
    lateinit var title: XSLFTextShape
    lateinit var text: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.text = titleText
            }
            if (ph.shapeName.contains("Text")) {
                text = ph
                text.text = subtitleText
            }
        }
    }
}
