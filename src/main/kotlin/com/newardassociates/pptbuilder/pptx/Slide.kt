package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.XSLFSlideLayout
import org.apache.poi.xslf.usermodel.XSLFTextShape

abstract class Slide(val deck: Deck, val layout: XSLFSlideLayout) {
    val slide = deck.ppt.createSlide(layout)
    val dimensions = deck.ppt.pageSize

    override fun toString(): String {
        return "${layout} - ${dimensions}"
    }

    /*
    TODO: Need to fix the dimensions on a Notes Slide when it's saved.
     */
    var notesBox: XSLFTextShape? = null
    var notes: String
        get() {
            return if (notesBox == null) notesBox!!.text else ""
        }
        set(value) {
            if (notesBox == null) {
                val notesSlide = deck.ppt.getNotesSlide(slide)
                notesBox = notesSlide.placeholders.filter { ph -> ph.shapeName.contains("Notes Placeholder") }[0]
                notesBox!!.clearText()
            }
            notesBox!!.text += value
        }
}
