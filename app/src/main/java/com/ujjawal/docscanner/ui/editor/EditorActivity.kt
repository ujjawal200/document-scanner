package com.ujjawal.docscanner.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File
import java.io.FileOutputStream

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var currentBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null
    private val document get() = DocumentHolder.document

    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val input = contentResolver.openInputStream(uri)
                    val cropped = android.graphics.BitmapFactory.decodeStream(input)
                    input?.close()
                    if (cropped != null) {
                        processedBitmap = cropped
                        binding.imagePreview.setImageBitmap(cropped)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentBitmap = ImageHolder.bitmap ?: run { finish(); return }
        ImageHolder.bitmap = null

        processImage()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCrop.setOnClickListener { launchCrop() }
        binding.btnOriginal.setOnClickListener { applyFilter(ImageFilters.FilterType.ORIGINAL) }
        binding.btnGrayscale.setOnClickListener { applyFilter(ImageFilters.FilterType.GRAYSCALE) }
        binding.btnBw.setOnClickListener { applyFilter(ImageFilters.FilterType.BW) }
        binding.btnEnhanced.setOnClickListener { applyFilter(ImageFilters.FilterType.ENHANCED) }

        binding.btnOcr.setOnClickListener { runOcr() }
        binding.btnAddPage.setOnClickListener { addPageAndGoBack() }
        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    private fun launchCrop() {
        val bitmap = processedBitmap ?: currentBitmap ?: return
        try {
            val file = File(cacheDir, "crop_temp.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

            val intent = Intent("com.android.camera.action.CROP").apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                putExtra("crop", "true")
                putExtra("return-data", false)
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            }
            if (intent.resolveActivity(packageManager) != null) {
                cropLauncher.launch(intent)
            } else {
                Toast.makeText(this, "No crop app available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Crop failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
