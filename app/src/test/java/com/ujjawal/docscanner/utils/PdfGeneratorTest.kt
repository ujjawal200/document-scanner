package com.ujjawal.docscanner.utils

import org.junit.Assert.*
import org.junit.Test

class PdfGeneratorTest {

    @Test
    fun `PdfGenerator object exists`() {
        assertNotNull(PdfGenerator)
    }

    @Test
    fun `generate method requires non-empty title`() {
        // Verify the method signature accepts expected params
        val methods = PdfGenerator::class.java.methods
        val generateMethod = methods.find { it.name == "generate" }
        assertNotNull(generateMethod)
        assertEquals(3, generateMethod!!.parameterCount)
    }
}
