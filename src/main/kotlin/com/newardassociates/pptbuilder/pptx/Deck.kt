package com.newardassociates.pptbuilder.pptx

import org.apache.poi.xslf.usermodel.SlideLayout
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout

class Deck(val ppt: XMLSlideShow) {
    constructor() : this(XMLSlideShow()) {}

    lateinit var titleLayout: XSLFSlideLayout
    lateinit var sectionHeaderLayout: XSLFSlideLayout
    lateinit var titleContentLayout: XSLFSlideLayout
    lateinit var titleOnlyLayout: XSLFSlideLayout
    lateinit var blankLayout: XSLFSlideLayout

    init {
        for (master in ppt.slideMasters) {
            for (layout in master.slideLayouts) {
                when (layout.type) {
                    SlideLayout.TITLE -> titleLayout = layout
                    SlideLayout.SECTION_HEADER -> sectionHeaderLayout = layout
                    SlideLayout.TITLE_AND_CONTENT -> titleContentLayout = layout
                    SlideLayout.TITLE_ONLY -> titleOnlyLayout = layout
                    SlideLayout.BLANK -> blankLayout = layout
                    else -> {
                    }
                }
            }
        }
    }

    val properties = ppt.properties

    var title: String
        get() {
            return properties.coreProperties.title
        }
        set(value) {
            properties.coreProperties.title = value; properties.commit()
        }

    var subject: String
        get() {
            return properties.coreProperties.subject
        }
        set(value) {
            properties.coreProperties.setSubjectProperty(value); properties.commit()
        }

    var author: String
        get() {
            return properties.coreProperties.creator
        }
        set(value) {
            properties.coreProperties.creator = value; properties.commit()
        }

    var affiliation: String
        get() {
            return properties.extendedProperties.company
        }
        set(value) {
            properties.extendedProperties.company = value; properties.commit()
        }

    var manager: String
        get() {
            return properties.extendedProperties.manager
        }
        set(value) {
            properties.extendedProperties.manager = value; properties.commit()
        }

    var description: String
        get() {
            return properties.coreProperties.description
        }
        set(value) {
            properties.coreProperties.description = value; properties.commit()
        }

    var category: String
        get() {
            return properties.coreProperties.category
        }
        set(value) {
            properties.coreProperties.category = value; properties.commit()
        }

    var keywords: String
        get() {
            return properties.coreProperties.keywords
        }
        set(value) {
            properties.coreProperties.keywords = value; properties.commit()
        }
}
