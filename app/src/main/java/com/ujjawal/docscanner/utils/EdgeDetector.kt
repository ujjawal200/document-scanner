package com.ujjawal.docscanner.utils

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object EdgeDetector {

    fun detectEdges(bitmap: Bitmap): List<PointF> {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        val edges = Mat()
        Imgproc.Canny(gray, edges, 75.0, 200.0)
        Imgproc.dilate(edges, edges, Mat(), Point(-1.0, -1.0), 1)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        contours.sortByDescending { Imgproc.contourArea(it) }

        for (contour in contours) {
            val approx = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())
            val peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true)

            if (approx.total() == 4L) {
                val points = approx.toArray()
                val sorted = sortCorners(points, bitmap.width.toFloat(), bitmap.height.toFloat())
                gray.release(); edges.release(); mat.release()
                return sorted
            }
        }

        gray.release(); edges.release(); mat.release()
        // Default to full image corners
        return listOf(
            PointF(0f, 0f),
            PointF(bitmap.width.toFloat(), 0f),
            PointF(bitmap.width.toFloat(), bitmap.height.toFloat()),
            PointF(0f, bitmap.height.toFloat())
        )
    }

    private fun sortCorners(points: Array<Point>, w: Float, h: Float): List<PointF> {
        val center = Point(points.map { it.x }.average(), points.map { it.y }.average())
        val topLeft = points.minByOrNull { it.x + it.y }!!
        val topRight = points.minByOrNull { (w - it.x) + it.y }!!
        val bottomRight = points.minByOrNull { (w - it.x) + (h - it.y) }!!
        val bottomLeft = points.minByOrNull { it.x + (h - it.y) }!!
        return listOf(
            PointF(topLeft.x.toFloat(), topLeft.y.toFloat()),
            PointF(topRight.x.toFloat(), topRight.y.toFloat()),
            PointF(bottomRight.x.toFloat(), bottomRight.y.toFloat()),
            PointF(bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
        )
    }
}
