package com.ujjawal.docscanner.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ujjawal.docscanner.R
import com.ujjawal.docscanner.databinding.ActivityHomeBinding
import com.ujjawal.docscanner.ui.camera.CameraActivity
import com.ujjawal.docscanner.ui.pdf.PdfPreviewActivity
import com.ujjawal.docscanner.utils.AppPrefs
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var multiSelectMode = false
    private val selectedFiles = mutableSetOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        setupMenu()
    }

    override fun onResume() {
        super.onResume()
        exitMultiSelect()
        loadDocuments()
    }

    private fun setupMenu() {
        binding.toolbar.setOnMenuItemClickListener { item -> handleMenu(item) }

        // Set checked states from prefs
        val menu = binding.toolbar.menu
        when (AppPrefs.getViewMode(this)) {
            "grid" -> menu.findItem(R.id.menu_view_grid)?.isChecked = true
            else -> menu.findItem(R.id.menu_view_list)?.isChecked = true
        }
        when (AppPrefs.getSortBy(this)) {
            "name" -> menu.findItem(R.id.menu_sort_name)?.isChecked = true
            "date_modified" -> menu.findItem(R.id.menu_sort_date_modified)?.isChecked = true
            else -> menu.findItem(R.id.menu_sort_date_added)?.isChecked = true
        }
        when (AppPrefs.getDefaultExport(this)) {
            "jpeg" -> menu.findItem(R.id.menu_export_jpeg)?.isChecked = true
            else -> menu.findItem(R.id.menu_export_pdf)?.isChecked = true
        }
    }

    private fun handleMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_view_list -> {
                item.isChecked = true
                AppPrefs.setViewMode(this, "list")
                loadDocuments()
            }
            R.id.menu_view_grid -> {
                item.isChecked = true
                AppPrefs.setViewMode(this, "grid")
                loadDocuments()
            }
            R.id.menu_select -> toggleMultiSelect()
            R.id.menu_delete_selected -> deleteSelected()
            R.id.menu_share_selected -> shareSelected()
            R.id.menu_sort_date_added -> {
                item.isChecked = true
                AppPrefs.setSortBy(this, "date_added")
                loadDocuments()
            }
            R.id.menu_sort_name -> {
                item.isChecked = true
                AppPrefs.setSortBy(this, "name")
                loadDocuments()
            }
            R.id.menu_sort_date_modified -> {
                item.isChecked = true
                AppPrefs.setSortBy(this, "date_modified")
                loadDocuments()
            }
            R.id.menu_export_pdf -> {
                item.isChecked = true
                AppPrefs.setDefaultExport(this, "pdf")
                Toast.makeText(this, "Default export: PDF", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_export_jpeg -> {
                item.isChecked = true
                AppPrefs.setDefaultExport(this, "jpeg")
                Toast.makeText(this, "Default export: JPEG", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_about -> showAbout()
            else -> return false
        }
        return true
    }

    private fun toggleMultiSelect() {
        if (multiSelectMode) {
            exitMultiSelect()
            loadDocuments()
        } else {
            multiSelectMode = true
            selectedFiles.clear()
            binding.toolbar.title = "Select Documents"
            binding.toolbar.menu.findItem(R.id.menu_select)?.title = "Cancel Selection"
            binding.toolbar.menu.findItem(R.id.menu_delete_selected)?.isVisible = true
            binding.toolbar.menu.findItem(R.id.menu_share_selected)?.isVisible = true
            loadDocuments()
        }
    }

    private fun exitMultiSelect() {
        multiSelectMode = false
        selectedFiles.clear()
        binding.toolbar.title = "Free Scanner"
        binding.toolbar.menu.findItem(R.id.menu_select)?.title = "Select Multiple"
        binding.toolbar.menu.findItem(R.id.menu_delete_selected)?.isVisible = false
        binding.toolbar.menu.findItem(R.id.menu_share_selected)?.isVisible = false
    }

    private fun deleteSelected() {
        if (selectedFiles.isEmpty()) return
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete ${selectedFiles.size} file(s)?")
            .setMessage("This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                selectedFiles.forEach { it.delete() }
                Toast.makeText(this, "${selectedFiles.size} deleted", Toast.LENGTH_SHORT).show()
                exitMultiSelect()
                loadDocuments()
            }
            .show()
    }

    private fun shareSelected() {
        if (selectedFiles.isEmpty()) return
        val uris = ArrayList(selectedFiles.map {
            androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", it)
        })
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uris[0])
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun showAbout() {
        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) { "1.0" }

        MaterialAlertDialogBuilder(this)
            .setTitle("Free Scanner")
            .setMessage("Version: $version\n\nA free, open-source document scanner.\nCapture, crop, filter, OCR, and export as PDF or JPEG.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadDocuments() {
        val dir = File(getExternalFilesDir(null), "Documents/DocScanner")
        val files = dir.listFiles()?.filter { it.extension == "pdf" || it.extension == "jpg" || it.extension == "jpeg" }
            ?: emptyList()

        val sorted = when (AppPrefs.getSortBy(this)) {
            "name" -> files.sortedBy { it.nameWithoutExtension.lowercase() }
            "date_modified" -> files.sortedByDescending { it.lastModified() }
            else -> files.sortedByDescending { it.lastModified() } // date_added ~ lastModified
        }

        if (sorted.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerDocs.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerDocs.visibility = View.VISIBLE

            val isGrid = AppPrefs.getViewMode(this) == "grid"
            binding.recyclerDocs.layoutManager = if (isGrid) {
                GridLayoutManager(this, 2)
            } else {
                LinearLayoutManager(this)
            }

            binding.recyclerDocs.adapter = DocAdapter(sorted, isGrid, multiSelectMode, selectedFiles) { file ->
                if (multiSelectMode) {
                    if (selectedFiles.contains(file)) selectedFiles.remove(file) else selectedFiles.add(file)
                    binding.toolbar.title = "${selectedFiles.size} selected"
                    binding.recyclerDocs.adapter?.notifyDataSetChanged()
                } else {
                    val intent = Intent(this, PdfPreviewActivity::class.java)
                    intent.putExtra("pdf_path", file.absolutePath)
                    startActivity(intent)
                }
            }
        }
    }
}

class DocAdapter(
    private val files: List<File>,
    private val isGrid: Boolean,
    private val multiSelect: Boolean,
    private val selected: Set<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<DocAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDocName)
        val tvDate: TextView? = view.findViewById(R.id.tvDocDate)
        val imgThumb: ImageView = view.findViewById(R.id.imgThumb)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (isGrid) R.layout.item_document_grid else R.layout.item_document
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = files[position]
        holder.tvName.text = file.nameWithoutExtension
        holder.tvDate?.let {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            it.text = sdf.format(Date(file.lastModified()))
        }
        holder.imgThumb.setImageResource(R.drawable.ic_camera)

        holder.cbSelect.visibility = if (multiSelect) View.VISIBLE else View.GONE
        holder.cbSelect.isChecked = selected.contains(file)
        holder.cbSelect.setOnClickListener { onClick(file) }
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount() = files.size
}
