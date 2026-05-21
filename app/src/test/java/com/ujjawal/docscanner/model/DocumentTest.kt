package com.ujjawal.docscanner.model

import android.graphics.Bitmap
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class DocumentTest {

    private fun mockBitmap(): Bitmap = mock(Bitmap::class.java)

    @Test
    fun `new document has empty pages`() {
        val doc = Document()
        assertTrue(doc.pages.isEmpty())
    }

    @Test
    fun `document title starts with Scan_ prefix`() {
        val doc = Document()
        assertTrue(doc.title.startsWith("Scan_"))
    }

    @Test
    fun `document title contains timestamp`() {
        val before = System.currentTimeMillis()
        val doc = Document()
        val after = System.currentTimeMillis()
        val timestamp = doc.title.removePrefix("Scan_").toLong()
        assertTrue(timestamp in before..after)
    }

    @Test
    fun `can add pages to document`() {
        val doc = Document()
        doc.pages.add(ScannedPage(originalBitmap = mockBitmap()))
        assertEquals(1, doc.pages.size)
    }

    @Test
    fun `custom title is preserved`() {
        val doc = Document(title = "MyDoc")
        assertEquals("MyDoc", doc.title)
    }

    @Test
    fun `scanned page defaults are correct`() {
        val page = ScannedPage(originalBitmap = mockBitmap())
        assertNull(page.croppedBitmap)
        assertNull(page.enhancedBitmap)
        assertTrue(page.corners.isEmpty())
        assertEquals("", page.ocrText)
    }

    @Test
    fun `multiple pages maintain order`() {
        val doc = Document()
        val bitmaps = List(3) { mockBitmap() }
        bitmaps.forEach { doc.pages.add(ScannedPage(originalBitmap = it)) }
        assertEquals(3, doc.pages.size)
        assertEquals(bitmaps[0], doc.pages[0].originalBitmap)
        assertEquals(bitmaps[2], doc.pages[2].originalBitmap)
    }
}
