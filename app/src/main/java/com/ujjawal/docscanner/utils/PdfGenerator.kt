package com.ujjawal.docscanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import java.io.ByteArrayOutputStream
import java.io.File

object PdfGenerator {

    fun generate(context: Context, pages: List<Bitmap>, title: String): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DocScanner")
        dir.mkdirs()
        val file = File(dir, "$title.pdf")

        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        pages.forEach { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageData = ImageDataFactory.create(stream.toByteArray())
            val image = Image(imageData)

            val pageSize = PageSize(image.imageWidth, image.imageHeight)
            pdf.addNewPage(pageSize)
            image.setFixedPosition(0f, 0f)
            document.add(image)
        }

        document.close()
        return file
    }
}
