package com.ujjawal.docscanner.ui.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class EdgeOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var corners: Array<PointF>? = null

    private val linePaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.argb(40, 0, 200, 255)
        style = Paint.Style.FILL
    }

    private val cornerPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setCorners(points: Array<PointF>?) {
        corners = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pts = corners ?: return
        if (pts.size < 4) return

        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            lineTo(pts[1].x, pts[1].y)
            lineTo(pts[2].x, pts[2].y)
            lineTo(pts[3].x, pts[3].y)
            close()
        }

        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, linePaint)

        // Draw corner handles
        for (pt in pts) {
            canvas.drawCircle(pt.x, pt.y, 12f, cornerPaint)
        }
    }
}
