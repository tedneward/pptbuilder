package com.newardassociates.pptbuilder.pptx

import com.newardassociates.pptbuilder.Processor
import org.apache.poi.xslf.usermodel.SlideLayout
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout
import java.util.logging.Logger

class Deck(val ppt: XMLSlideShow) {
    private val logger = Logger.getLogger(Deck::class.java.canonicalName)

    constructor() : this(XMLSlideShow()) {}

    lateinit var titleLayout: XSLFSlideLayout
    lateinit var sectionHeaderLayout: XSLFSlideLayout
    lateinit var titleContentLayout: XSLFSlideLayout
    lateinit var titleOnlyLayout: XSLFSlideLayout
    lateinit var blankLayout: XSLFSlideLayout

    init {
        logger.info("Iterating through layouts in ${ppt}")
        for (master in ppt.slideMasters) {
            logger.info("Iterating through layouts in ${master}")
            for (layout in master.slideLayouts) {
                logger.info("Found '${layout.name}'/(${layout.type})")
                when (layout.type) {
                    SlideLayout.TITLE -> titleLayout = layout
                    SlideLayout.SECTION_HEADER -> sectionHeaderLayout = layout
                    SlideLayout.TITLE_AND_CONTENT -> titleContentLayout = layout
                    SlideLayout.TITLE_ONLY -> titleOnlyLayout = layout
                    SlideLayout.BLANK -> blankLayout = layout
                    else -> {
                        logger.info("... unrecognized layout: '${layout.name}'")
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
            logger.fine("Setting title property to $value")
            properties.coreProperties.title = value; properties.commit()
        }

    var subject: String
        get() {
            return properties.coreProperties.subject
        }
        set(value) {
            logger.fine("Setting subject property to $value")
            properties.coreProperties.setSubjectProperty(value); properties.commit()
        }

    var author: String
        get() {
            return properties.coreProperties.creator
        }
        set(value) {
            logger.fine("Setting author property to $value")
            properties.coreProperties.creator = value; properties.commit()
        }

    var affiliation: String
        get() {
            return properties.extendedProperties.company
        }
        set(value) {
            logger.fine("Setting affiliation property to $value")
            properties.extendedProperties.company = value; properties.commit()
        }

    var manager: String
        get() {
            return properties.extendedProperties.manager
        }
        set(value) {
            logger.fine("Setting manager property to $value")
            properties.extendedProperties.manager = value; properties.commit()
        }

    var description: String
        get() {
            return properties.coreProperties.description
        }
        set(value) {
            logger.fine("Setting description property to $value")
            properties.coreProperties.description = value; properties.commit()
        }

    var category: String
        get() {
            return properties.coreProperties.category
        }
        set(value) {
            logger.fine("Setting category property to $value")
            properties.coreProperties.category = value; properties.commit()
        }

    var keywords: String
        get() {
            return properties.coreProperties.keywords
        }
        set(value) {
            logger.fine("Setting keywords property to $value")
            properties.coreProperties.keywords = value; properties.commit()
        }
}
