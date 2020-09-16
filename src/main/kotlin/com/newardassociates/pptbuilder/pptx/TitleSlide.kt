package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.XSLFTextShape

/*
Discovering layout Title Slide
	Title Slide has placeholder Title 1
	Title Slide has placeholder Subtitle 2
	Title Slide has placeholder Date Placeholder 3
	Title Slide has placeholder Footer Placeholder 4
	Title Slide has placeholder Slide Number Placeholder 5
 */
class TitleSlide(deck: Deck, titleText: String, subtitleText: String = "")
    : Slide(deck, deck.titleLayout) {
    lateinit var title: XSLFTextShape
    lateinit var subtitle: XSLFTextShape

    init {
        for (ph in slide.placeholders) {
            if (ph.shapeName.contains("Title")) {
                title = ph
                title.clearText()
                title.text = titleText
            }
            if (ph.shapeName.contains("Subtitle")) {
                subtitle = ph
                subtitle.clearText()
                subtitle.text = subtitleText
            }
        }
    }
}
