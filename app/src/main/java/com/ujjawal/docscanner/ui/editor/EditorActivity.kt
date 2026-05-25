package com.ujjawal.docscanner.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.view.View
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
import java.io.File
import java.io.FileOutputStream

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var originalBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null
    private val document get() = DocumentHolder.document

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        originalBitmap = ImageHolder.bitmap ?: run { finish(); return }
        ImageHolder.bitmap = null

        // Show original image and detect edges for crop overlay
        binding.imagePreview.setImageBitmap(originalBitmap)
        showCropMode()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCrop.setOnClickListener { resetCrop() }
        binding.btnConfirmCrop.setOnClickListener { applyCrop() }

        binding.btnOriginal.setOnClickListener { applyFilter(ImageFilters.FilterType.ORIGINAL) }
        binding.btnGrayscale.setOnClickListener { applyFilter(ImageFilters.FilterType.GRAYSCALE) }
        binding.btnBw.setOnClickListener { applyFilter(ImageFilters.FilterType.BW) }
        binding.btnEnhanced.setOnClickListener { applyFilter(ImageFilters.FilterType.ENHANCED) }

        binding.btnOcr.setOnClickListener { runOcr() }
        binding.btnAddPage.setOnClickListener { addPageAndGoBack() }
        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    private fun showCropMode() {
        binding.tvTitle.text = "Adjust Crop"
        binding.cropOverlay.visibility = View.VISIBLE
        binding.btnConfirmCrop.visibility = View.VISIBLE
        binding.filterBar.visibility = View.GONE
        binding.actionBar.visibility = View.GONE

        // Detect edges and set corners on overlay after layout
        binding.cropOverlay.post {
            lifecycleScope.launch(Dispatchers.Default) {
                val bitmap = originalBitmap!!
                val corners = EdgeDetector.detectEdges(bitmap)

                // Scale corners from bitmap coords to overlay view coords
                val overlayW = binding.cropOverlay.width.toFloat()
                val overlayH = binding.cropOverlay.height.toFloat()

                // Calculate the fitCenter scaling used by ImageView
                val scaleX = overlayW / bitmap.width
                val scaleY = overlayH / bitmap.height
                val scale = minOf(scaleX, scaleY)
                val offsetX = (overlayW - bitmap.width * scale) / 2f
                val offsetY = (overlayH - bitmap.height * scale) / 2f

                val scaled = corners.map { pt ->
                    PointF(pt.x * scale + offsetX, pt.y * scale + offsetY)
                }

                withContext(Dispatchers.Main) {
                    binding.cropOverlay.setCorners(scaled)
                }
            }
        }
    }

    private fun resetCrop() {
        binding.cropOverlay.setDefaultCorners()
    }

    private fun applyCrop() {
        val bitmap = originalBitmap ?: return

        lifecycleScope.launch(Dispatchers.Default) {
            // Convert overlay corners back to bitmap coordinates
            val overlayW = binding.cropOverlay.width.toFloat()
            val overlayH = binding.cropOverlay.height.toFloat()
            val scaleX = overlayW / bitmap.width
            val scaleY = overlayH / bitmap.height
            val scale = minOf(scaleX, scaleY)
            val offsetX = (overlayW - bitmap.width * scale) / 2f
            val offsetY = (overlayH - bitmap.height * scale) / 2f

            val viewCorners = binding.cropOverlay.getCorners()
            val bitmapCorners = viewCorners.map { pt ->
                PointF((pt.x - offsetX) / scale, (pt.y - offsetY) / scale)
            }

            val cropped = PerspectiveTransform.transform(bitmap, bitmapCorners)
            croppedBitmap = cropped
            processedBitmap = cropped

            withContext(Dispatchers.Main) {
                binding.imagePreview.setImageBitmap(cropped)
                showEditMode()
            }
        }
    }

    private fun showEditMode() {
        binding.tvTitle.text = "Edit Scan"
        binding.cropOverlay.visibility = View.GONE
        binding.btnConfirmCrop.visibility = View.GONE
        binding.filterBar.visibility = View.VISIBLE
        binding.actionBar.visibility = View.VISIBLE

        // Auto-apply default color filter from settings
        val defaultFilter = AppPrefs.getColorFilter(this)
        if (defaultFilter != "ORIGINAL") {
            val filterType = when (defaultFilter) {
                "GRAYSCALE" -> ImageFilters.FilterType.GRAYSCALE
                "BW" -> ImageFilters.FilterType.BW
                "ENHANCED" -> ImageFilters.FilterType.ENHANCED
                else -> null
            }
            filterType?.let { applyFilter(it) }
        }
    }

    private fun applyFilter(filter: ImageFilters.FilterType) {
        val source = croppedBitmap ?: originalBitmap ?: return
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
                binding.txtOcrResult.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@EditorActivity, "No text detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPageAndGoBack() {
        processedBitmap?.let {
            document.pages.add(ScannedPage(originalBitmap!!, croppedBitmap = it))
            Toast.makeText(this, "Page ${document.pages.size} added", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun exportPdf() {
        processedBitmap?.let {
            document.pages.add(ScannedPage(originalBitmap!!, croppedBitmap = it))
        }
        if (document.pages.isEmpty()) {
            Toast.makeText(this, "No pages to export", Toast.LENGTH_SHORT).show()
            return
        }
        showShareDialog()
    }

    private fun showShareDialog() {
        val dialogView = layoutInflater.inflate(com.ujjawal.docscanner.R.layout.dialog_share, null)
        val etFileName = dialogView.findViewById<android.widget.EditText>(com.ujjawal.docscanner.R.id.etFileName)
        val tvFileInfo = dialogView.findViewById<android.widget.TextView>(com.ujjawal.docscanner.R.id.tvFileInfo)
        val toggleFormat = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(com.ujjawal.docscanner.R.id.toggleFormat)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.ujjawal.docscanner.R.id.btnCancel)
        val btnShare = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.ujjawal.docscanner.R.id.btnShare)
        val btnEditName = dialogView.findViewById<android.widget.ImageButton>(com.ujjawal.docscanner.R.id.btnEditName)

        etFileName.setText(document.title.ifBlank { "document_scan" })
        tvFileInfo.text = "${document.pages.size} Files"
        val defaultExport = AppPrefs.getDefaultExport(this)
        toggleFormat.check(if (defaultExport == "jpeg") com.ujjawal.docscanner.R.id.btnJpeg else com.ujjawal.docscanner.R.id.btnPdf)

        btnEditName.setOnClickListener {
            etFileName.requestFocus()
            etFileName.setSelection(etFileName.text.length)
        }

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnShare.setOnClickListener {
            val rawName = etFileName.text.toString().trim().ifBlank { "document_scan" }
            val fileName = rawName.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").trim().ifBlank { "document_scan" }
            val asPdf = toggleFormat.checkedButtonId == com.ujjawal.docscanner.R.id.btnPdf
            dialog.dismiss()
            performExport(fileName, asPdf)
        }
        dialog.show()
    }

    private fun performExport(fileName: String, asPdf: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmaps = document.pages.mapNotNull { it.croppedBitmap }
            if (asPdf) {
                val file = PdfGenerator.generate(this@EditorActivity, bitmaps, fileName)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditorActivity, "PDF saved: ${file.name}", Toast.LENGTH_LONG).show()
                    DocumentHolder.reset()
                    val intent = Intent(this@EditorActivity, PdfPreviewActivity::class.java)
                    intent.putExtra("pdf_path", file.absolutePath)
                    startActivity(intent)
                }
            } else {
                val dir = File(getExternalFilesDir(null), "Documents/DocScanner")
                dir.mkdirs()
                val files = bitmaps.mapIndexed { i, bmp ->
                    val f = File(dir, "${fileName}_${i + 1}.jpg")
                    FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                    f
                }
                withContext(Dispatchers.Main) {
                    DocumentHolder.reset()
                    val uris = ArrayList(files.map {
                        androidx.core.content.FileProvider.getUriForFile(this@EditorActivity, "$packageName.fileprovider", it)
                    })
                    val shareIntent = if (files.size == 1) {
                        Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, uris[0])
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    } else {
                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/jpeg"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share"))
                }
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
