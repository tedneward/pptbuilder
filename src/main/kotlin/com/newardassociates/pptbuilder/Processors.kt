package com.newardassociates.pptbuilder

open class Processor(val choice : String) {
    fun process(presentation : Presentation) : Unit { }
}

class PPTXProcessor : Processor("pptx") {

}
class HTMLProcessor : Processor("html") {

}
class PDFProcessor : Processor("pdf") {

}
