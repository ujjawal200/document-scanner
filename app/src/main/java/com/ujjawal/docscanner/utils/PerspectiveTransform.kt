package com.ujjawal.docscanner.utils

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object PerspectiveTransform {

    fun transform(bitmap: Bitmap, corners: List<PointF>): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val tl = corners[0]
        val tr = corners[1]
        val br = corners[2]
        val bl = corners[3]

        val widthA = Math.sqrt(((br.x - bl.x) * (br.x - bl.x) + (br.y - bl.y) * (br.y - bl.y)).toDouble())
        val widthB = Math.sqrt(((tr.x - tl.x) * (tr.x - tl.x) + (tr.y - tl.y) * (tr.y - tl.y)).toDouble())
        val maxWidth = maxOf(widthA, widthB).toInt()

        val heightA = Math.sqrt(((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y)).toDouble())
        val heightB = Math.sqrt(((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y)).toDouble())
        val maxHeight = maxOf(heightA, heightB).toInt()

        val src = MatOfPoint2f(
            Point(tl.x.toDouble(), tl.y.toDouble()),
            Point(tr.x.toDouble(), tr.y.toDouble()),
            Point(br.x.toDouble(), br.y.toDouble()),
            Point(bl.x.toDouble(), bl.y.toDouble())
        )

        val dst = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth.toDouble(), 0.0),
            Point(maxWidth.toDouble(), maxHeight.toDouble()),
            Point(0.0, maxHeight.toDouble())
        )

        val transformMatrix = Imgproc.getPerspectiveTransform(src, dst)
        val warped = Mat()
        Imgproc.warpPerspective(mat, warped, transformMatrix, Size(maxWidth.toDouble(), maxHeight.toDouble()))

        val result = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(warped, result)

        mat.release(); warped.release(); transformMatrix.release()
        return result
    }
}
