package com.phonecleaner.storagecleaner.cache.utils.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.extension.dp2px

class AnalyticsProgressBar : View {
    private val PROGRESS_WIDTH = 24f
    private lateinit var listColor: Array<String>
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var viewRect = RectF()
    private var centerPoint = PointF()
    private var viewPadding = 0f
    private var progress = intArrayOf(0, 0, 0, 0, 100)

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    private fun initView(context: Context) {
        viewPadding = context.dp2px(PROGRESS_WIDTH).toFloat()
        listColor = context.resources.getStringArray(R.array.progressColor)
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = viewPadding
    }

    fun setProgress(data: IntArray) {
        this.progress = data
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(
            viewPadding / 2,
            viewPadding / 2,
            w.toFloat() - viewPadding / 2,
            (h.toFloat() * 2) - viewPadding / 2
        )
        centerPoint.set(w / 2f, h / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawProgress(canvas)
    }

    private fun drawProgress(canvas: Canvas) {
        var lastAngle = VIEW_START_ANGLE
        progress.forEachIndexed { index, progress ->
            val start = lastAngle + progress.toFloat() / 100f * 180
            paint.color = Color.parseColor(listColor[index])
            canvas.drawArc(viewRect, lastAngle, start - VIEW_START_ANGLE, false, paint)
            lastAngle = start
        }
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = Color.parseColor(listColor.last())
        canvas.drawArc(viewRect, 180f, 180f, false, paint)
    }

    companion object {
        const val VIEW_START_ANGLE = 180f
    }
}

