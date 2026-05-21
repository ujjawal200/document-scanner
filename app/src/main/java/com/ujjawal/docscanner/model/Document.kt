package com.ujjawal.docscanner.model

import android.graphics.Bitmap
import android.graphics.PointF

data class ScannedPage(
    val originalBitmap: Bitmap,
    var croppedBitmap: Bitmap? = null,
    var enhancedBitmap: Bitmap? = null,
    var corners: List<PointF> = emptyList(),
    var ocrText: String = ""
)

data class Document(
    val pages: MutableList<ScannedPage> = mutableListOf(),
    var title: String = "Scan_${System.currentTimeMillis()}"
)
