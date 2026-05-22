package com.ujjawal.docscanner.ui.editor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val corners = arrayOf(PointF(), PointF(), PointF(), PointF())
    private var activeCorner = -1
    private val touchRadius = 48f

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

    fun setCorners(points: List<PointF>) {
        if (points.size >= 4) {
            for (i in 0..3) {
                corners[i].set(points[i].x, points[i].y)
            }
            invalidate()
        }
    }

    fun getCorners(): List<PointF> = corners.map { PointF(it.x, it.y) }

    fun setDefaultCorners() {
        val w = width.toFloat()
        val h = height.toFloat()
        val margin = 40f
        corners[0].set(margin, margin)
        corners[1].set(w - margin, margin)
        corners[2].set(w - margin, h - margin)
        corners[3].set(margin, h - margin)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Set default if corners are at 0,0
        if (corners[0].x == 0f && corners[0].y == 0f) {
            setDefaultCorners()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val path = Path().apply {
            moveTo(corners[0].x, corners[0].y)
            lineTo(corners[1].x, corners[1].y)
            lineTo(corners[2].x, corners[2].y)
            lineTo(corners[3].x, corners[3].y)
            close()
        }

        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, linePaint)

        for (pt in corners) {
            canvas.drawCircle(pt.x, pt.y, 14f, cornerPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeCorner = findNearestCorner(event.x, event.y)
                return activeCorner >= 0
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeCorner >= 0) {
                    corners[activeCorner].set(
                        event.x.coerceIn(0f, width.toFloat()),
                        event.y.coerceIn(0f, height.toFloat())
                    )
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                activeCorner = -1
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findNearestCorner(x: Float, y: Float): Int {
        var minDist = Float.MAX_VALUE
        var idx = -1
        for (i in corners.indices) {
            val d = Math.hypot((corners[i].x - x).toDouble(), (corners[i].y - y).toDouble()).toFloat()
            if (d < touchRadius && d < minDist) {
                minDist = d
                idx = i
            }
        }
        return idx
    }
}
