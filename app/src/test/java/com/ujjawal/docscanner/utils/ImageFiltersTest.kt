package com.ujjawal.docscanner.utils

import android.graphics.Bitmap
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class ImageFiltersTest {

    @Test
    fun `FilterType has four values`() {
        val types = ImageFilters.FilterType.values()
        assertEquals(4, types.size)
    }

    @Test
    fun `FilterType contains expected entries`() {
        assertNotNull(ImageFilters.FilterType.valueOf("ORIGINAL"))
        assertNotNull(ImageFilters.FilterType.valueOf("GRAYSCALE"))
        assertNotNull(ImageFilters.FilterType.valueOf("BW"))
        assertNotNull(ImageFilters.FilterType.valueOf("ENHANCED"))
    }

    @Test
    fun `ORIGINAL filter returns same bitmap`() {
        val bitmap = mock(Bitmap::class.java)
        val result = ImageFilters.apply(bitmap, ImageFilters.FilterType.ORIGINAL)
        assertSame(bitmap, result)
    }
}
