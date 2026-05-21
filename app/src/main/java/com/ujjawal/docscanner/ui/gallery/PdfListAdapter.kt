package com.ujjawal.docscanner.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ujjawal.docscanner.databinding.ItemPdfBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PdfListAdapter(
    private val files: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<PdfListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPdfBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPdfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.binding.txtFileName.text = file.nameWithoutExtension
        holder.binding.txtDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(file.lastModified()))
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount() = files.size
}
