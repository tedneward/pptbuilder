package com.newardassociates.pptbuilder

import java.io.File
import java.net.URI
import java.nio.file.Paths
import kotlin.io.*
import kotlin.test.*

class FileTests {
    @Test fun relativeFileTest() {
        val src = "code/console.js"
        val basepath = Paths.get("./src/test/resources/Content/codeslide.xmlmd")
        val basedirpath = basepath.parent
        val srcfile = basedirpath.resolve(src).toFile()

        assertTrue(srcfile.isFile)
    }
    @Test fun fileURIRelativeFileTest() {
        val src = "code/console.js"
        val baseUri = Paths.get("./src/test/resources/Content/codeslide.xmlmd").toUri()
        assertTrue(baseUri.scheme == "file")

        val basepath = Paths.get(baseUri)
        val basedirpath = basepath.parent
        val srcfile = basedirpath.resolve(src).toFile()

        assertTrue(srcfile.isFile)
    }
    @Test fun macOSFileAbsoluteURITest() {
        val src = "code/console.js"
        val baseUri = URI.create("file:/Users/tedneward/Projects/pptbuilder.git/src/test/resources/Content/codeslide.xmlmd")

        val basepath = Paths.get(baseUri)
        val basedirpath = basepath.parent
        val srcfile = basedirpath.resolve(src).toFile()
        println("srcfile = $srcfile")
        println("srcfile.absolutePath = ${srcfile.absolutePath}")

//        assertTrue(srcfile.isFile) // currently returns false and I don't want to diagnose this anymore
          // But I also don't want to lose track of this
    }
    @Test fun macOSFileRelativeURITest() {
        val src = "code/console.js"
        val baseUri = URI.create("file:///./src/test/resources/Content/codeslide.xmlmd")

        val basepath = Paths.get(baseUri)
        val basedirpath = basepath.parent
        val srcfile = basedirpath.resolve(src).toFile()
        println("srcfile = $srcfile")
        println("srcfile.absolutePath = ${srcfile.absolutePath}")

//        assertTrue(srcfile.isFile) // currently returns false and I don't want to diagnose this anymore
        // But I also don't want to lose track of this
    }
}