package com.ujjawal.docscanner.ui.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ujjawal.docscanner.databinding.ActivityPdfPreviewBinding
import java.io.File

class PdfPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val path = intent.getStringExtra("pdf_path") ?: run { finish(); return }
        val file = File(path)

        binding.txtPdfName.text = file.nameWithoutExtension
        binding.txtPdfInfo.text = "Size: ${file.length() / 1024} KB"

        binding.btnShare.setOnClickListener { sharePdf(file) }
        binding.btnDelete.setOnClickListener {
            file.delete()
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share PDF"))
    }
}
