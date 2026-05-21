package com.ujjawal.docscanner.utils

import org.junit.Assert.*
import org.junit.Test

class EdgeDetectorTest {

    @Test
    fun `EdgeDetector object exists`() {
        assertNotNull(EdgeDetector)
    }

    @Test
    fun `detectEdges method exists with correct signature`() {
        val methods = EdgeDetector::class.java.methods
        val method = methods.find { it.name == "detectEdges" }
        assertNotNull(method)
        assertEquals(1, method!!.parameterCount)
    }
}

class PerspectiveTransformTest {

    @Test
    fun `PerspectiveTransform object exists`() {
        assertNotNull(PerspectiveTransform)
    }

    @Test
    fun `transform method exists with correct signature`() {
        val methods = PerspectiveTransform::class.java.methods
        val method = methods.find { it.name == "transform" }
        assertNotNull(method)
        assertEquals(2, method!!.parameterCount)
    }
}
