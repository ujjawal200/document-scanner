package com.ujjawal.docscanner.ui.editor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ujjawal.docscanner.databinding.ActivityEditorBinding
import com.ujjawal.docscanner.model.Document
import com.ujjawal.docscanner.model.ScannedPage
import com.ujjawal.docscanner.ui.camera.ImageHolder
import com.ujjawal.docscanner.ui.pdf.PdfPreviewActivity
import com.ujjawal.docscanner.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var currentBitmap: android.graphics.Bitmap? = null
    private var processedBitmap: android.graphics.Bitmap? = null
    private val document get() = DocumentHolder.document

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentBitmap = ImageHolder.bitmap ?: run { finish(); return }
        ImageHolder.bitmap = null // Release reference

        processImage()

        binding.btnOriginal.setOnClickListener { applyFilter(ImageFilters.FilterType.ORIGINAL) }
        binding.btnGrayscale.setOnClickListener { applyFilter(ImageFilters.FilterType.GRAYSCALE) }
        binding.btnBw.setOnClickListener { applyFilter(ImageFilters.FilterType.BW) }
        binding.btnEnhanced.setOnClickListener { applyFilter(ImageFilters.FilterType.ENHANCED) }

        binding.btnOcr.setOnClickListener { runOcr() }
        binding.btnAddPage.setOnClickListener { addPageAndGoBack() }
        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    private fun processImage() {
        lifecycleScope.launch(Dispatchers.Default) {
            val bitmap = currentBitmap!!
            val corners = EdgeDetector.detectEdges(bitmap)
            val cropped = PerspectiveTransform.transform(bitmap, corners)
            processedBitmap = cropped

            withContext(Dispatchers.Main) {
                binding.imagePreview.setImageBitmap(cropped)
            }
        }
    }

    private fun applyFilter(filter: ImageFilters.FilterType) {
        val source = processedBitmap ?: currentBitmap ?: return
        lifecycleScope.launch(Dispatchers.Default) {
            val filtered = ImageFilters.apply(source, filter)
            withContext(Dispatchers.Main) {
                binding.imagePreview.setImageBitmap(filtered)
                processedBitmap = filtered
            }
        }
    }

    private fun runOcr() {
        val bitmap = processedBitmap ?: return
        lifecycleScope.launch {
            val text = withContext(Dispatchers.Default) { OcrEngine.extractText(bitmap) }
            if (text.isNotBlank()) {
                binding.txtOcrResult.text = text
                binding.txtOcrResult.visibility = android.view.View.VISIBLE
            } else {
                Toast.makeText(this@EditorActivity, "No text detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPageAndGoBack() {
        processedBitmap?.let {
            document.pages.add(ScannedPage(currentBitmap!!, croppedBitmap = it))
            Toast.makeText(this, "Page ${document.pages.size} added", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun exportPdf() {
        processedBitmap?.let {
            document.pages.add(ScannedPage(currentBitmap!!, croppedBitmap = it))
        }
        if (document.pages.isEmpty()) {
            Toast.makeText(this, "No pages to export", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmaps = document.pages.mapNotNull { it.croppedBitmap }
            val file = PdfGenerator.generate(this@EditorActivity, bitmaps, document.title)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditorActivity, "PDF saved: ${file.name}", Toast.LENGTH_LONG).show()
                DocumentHolder.reset() // Clear for next session
                val intent = Intent(this@EditorActivity, PdfPreviewActivity::class.java)
                intent.putExtra("pdf_path", file.absolutePath)
                startActivity(intent)
            }
        }
    }
}

object DocumentHolder {
    var document = Document()
        private set

    fun reset() {
        document = Document()
    }
}
