package com.ujjawal.docscanner.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ujjawal.docscanner.R
import com.ujjawal.docscanner.databinding.ActivityHomeBinding
import com.ujjawal.docscanner.ui.camera.CameraActivity
import com.ujjawal.docscanner.ui.pdf.PdfPreviewActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.recyclerDocs.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        loadDocuments()
    }

    private fun loadDocuments() {
        val dir = File(getExternalFilesDir(null), "Documents/DocScanner")
        val files = dir.listFiles()?.filter { it.extension == "pdf" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (files.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerDocs.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerDocs.visibility = View.VISIBLE
            binding.recyclerDocs.adapter = DocAdapter(files) { file ->
                val intent = Intent(this, PdfPreviewActivity::class.java)
                intent.putExtra("pdf_path", file.absolutePath)
                startActivity(intent)
            }
        }
    }
}

class DocAdapter(
    private val files: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<DocAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDocName)
        val tvDate: TextView = view.findViewById(R.id.tvDocDate)
        val imgThumb: ImageView = view.findViewById(R.id.imgThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = files[position]
        holder.tvName.text = file.nameWithoutExtension
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(file.lastModified()))
        holder.imgThumb.setImageResource(R.drawable.ic_camera)
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount() = files.size
}
