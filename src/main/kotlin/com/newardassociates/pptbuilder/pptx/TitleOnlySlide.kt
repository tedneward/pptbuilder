package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.XSLFTextShape

/*
Discovering layout Title Only
	Title Only has placeholder Title 1
	Title Only has placeholder Date Placeholder 2
	Title Only has placeholder Footer Placeholder 3
	Title Only has placeholder Slide Number Placeholder 4
 */
open class TitleOnlySlide(deck: Deck, titleText: String)
    : Slide(deck, deck.titleOnlyLayout) {
    lateinit var title: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.text = titleText
            }
        }
    }
}
