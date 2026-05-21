package com.ujjawal.docscanner.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ImageFilters {

    enum class FilterType { ORIGINAL, GRAYSCALE, BW, ENHANCED }

    fun apply(bitmap: Bitmap, filter: FilterType): Bitmap {
        if (filter == FilterType.ORIGINAL) return bitmap

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val result = when (filter) {
            FilterType.GRAYSCALE -> {
                val gray = Mat()
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
                val rgba = Mat()
                Imgproc.cvtColor(gray, rgba, Imgproc.COLOR_GRAY2RGBA)
                gray.release()
                rgba
            }
            FilterType.BW -> {
                val gray = Mat()
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
                val bw = Mat()
                Imgproc.adaptiveThreshold(gray, bw, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
                val rgba = Mat()
                Imgproc.cvtColor(bw, rgba, Imgproc.COLOR_GRAY2RGBA)
                gray.release(); bw.release()
                rgba
            }
            FilterType.ENHANCED -> {
                val lab = Mat()
                Imgproc.cvtColor(mat, lab, Imgproc.COLOR_BGR2Lab)
                val channels = mutableListOf<Mat>()
                org.opencv.core.Core.split(lab, channels)
                val clahe = Imgproc.createCLAHE(2.0, org.opencv.core.Size(8.0, 8.0))
                clahe.apply(channels[0], channels[0])
                org.opencv.core.Core.merge(channels, lab)
                val enhanced = Mat()
                Imgproc.cvtColor(lab, enhanced, Imgproc.COLOR_Lab2BGR)
                val rgba = Mat()
                Imgproc.cvtColor(enhanced, rgba, Imgproc.COLOR_BGR2RGBA)
                channels.forEach { it.release() }
                lab.release(); enhanced.release()
                rgba
            }
            else -> mat
        }

        val output = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, output)
        mat.release(); result.release()
        return output
    }
}
