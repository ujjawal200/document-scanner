package com.ujjawal.docscanner.ui.gallery

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ujjawal.docscanner.databinding.ActivityGalleryBinding
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DocScanner")
        val pdfFiles = dir.listFiles { f -> f.extension == "pdf" }?.toList() ?: emptyList()

        if (pdfFiles.isEmpty()) {
            Toast.makeText(this, "No scanned documents yet", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = PdfListAdapter(pdfFiles) { file ->
            val intent = android.content.Intent(this, com.ujjawal.docscanner.ui.pdf.PdfPreviewActivity::class.java)
            intent.putExtra("pdf_path", file.absolutePath)
            startActivity(intent)
        }
    }
}
