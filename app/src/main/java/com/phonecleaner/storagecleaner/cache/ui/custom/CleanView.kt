package com.phonecleaner.storagecleaner.cache.ui.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils

class CleanView : View {
    private val viewRect = RectF()
    private val centerPoint = PointF()
    private val circle1Rect = RectF()
    private val circle2Rect = RectF()
    private val circle3Rect = RectF()
    private val circle4Rect = RectF()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    private var viewType = ViewType.NoJunk

    fun setViewType(viewType: ViewType) {
        this.viewType = viewType
        listCircle.forEach {
            it.color = this.viewType.color
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(0f, 0f, w.toFloat(), h.toFloat())
        centerPoint.set(w / 2f, h / 2f)

        val circle1Radius = CIRCLE_1_RADIUS.toPx
        circle1Rect.set(
            centerPoint.x - circle1Radius,
            centerPoint.y - circle1Radius,
            centerPoint.x + circle1Radius,
            centerPoint.y + circle1Radius
        )

        val circle2Radius = CIRCLE_2_RADIUS.toPx
        circle2Rect.set(
            centerPoint.x - circle2Radius,
            centerPoint.y - circle2Radius,
            centerPoint.x + circle2Radius,
            centerPoint.y + circle2Radius
        )

        val circle3Radius = CIRCLE_3_RADIUS.toPx
        circle3Rect.set(
            centerPoint.x - circle3Radius,
            centerPoint.y - circle3Radius,
            centerPoint.x + circle3Radius,
            centerPoint.y + circle3Radius
        )

        val circle4Radius = CIRCLE_4_RADIUS.toPx
        circle4Rect.set(
            centerPoint.x - circle4Radius,
            centerPoint.y - circle4Radius,
            centerPoint.x + circle4Radius,
            centerPoint.y + circle4Radius
        )

        if (w > 0 && h > 0) {
            listCircle.forEach {
                val padding = centerPoint.x - it.radius
                val anim = ValueAnimator.ofFloat(1f, 0f)
                anim?.addUpdateListener { animation ->
                    listCircle.forEach {
                        it.displayRadius += 0.1f
                        val alpha =
                            (CIRCLE_RADIUS_MAX.toPx - it.displayRadius) / (CIRCLE_RADIUS_MAX.toPx - CIRCLE_4_RADIUS.toPx)
                        it.displayColor =
                            ColorUtils.setAlphaComponent(it.color, (alpha * 255).toInt())
                        if (it.displayRadius >= CIRCLE_RADIUS_MAX.toPx) {
                            it.indexDraw = it.indexDraw + 1
                            it.radius = CIRCLE_4_RADIUS.toPx
                            it.displayRadius = it.radius
                            it.displayColor = it.color
                        }
                    }
                    invalidate()
                }
                anim?.repeatCount = ValueAnimator.INFINITE
                anim?.duration = 3000
                anim?.start()
            }
        }
    }

    private var listCircle = listOf(
        Circle(
            indexDraw = 3, radius = CIRCLE_4_RADIUS.toPx, color = viewType.color
        ),
        Circle(
            indexDraw = 2, radius = CIRCLE_5_RADIUS.toPx, color = viewType.color
        ),
        Circle(
            indexDraw = 1, radius = CIRCLE_6_RADIUS.toPx, color = viewType.color
        ),
    )

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->
            paint.shader = null
            paint.style = Paint.Style.FILL

            val list = listCircle.sortedBy { it.indexDraw }
            list.forEach {
                paint.color = it.displayColor
                canvas.drawCircle(centerPoint.x, centerPoint.y, it.displayRadius, paint)
            }

            paint.shader = null
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerPoint.x, centerPoint.y, CIRCLE_3_RADIUS.toPx, paint)

            paint.shader = LinearGradient(
                0f,
                0f,
                circle2Rect.width(),
                0f,
                Color.parseColor("#F8F6F6"),
                Color.parseColor("#F1F0F0"),
                Shader.TileMode.MIRROR
            )
            canvas.drawCircle(centerPoint.x, centerPoint.y, CIRCLE_2_RADIUS.toPx + 1f.toPx, paint)

            paint.shader = LinearGradient(
                0f,
                0f,
                circle1Rect.width(),
                0f,
                Color.parseColor("#F5F3F3"),
                Color.WHITE,
                Shader.TileMode.MIRROR
            )
            canvas.drawCircle(centerPoint.x, centerPoint.y, CIRCLE_1_RADIUS.toPx, paint)

        }
    }

    companion object {
        const val CIRCLE_1_RADIUS = 40f
        const val CIRCLE_2_RADIUS = 48f
        const val CIRCLE_3_RADIUS = 52f
        const val CIRCLE_4_RADIUS = 65f
        const val CIRCLE_5_RADIUS = 75f
        const val CIRCLE_6_RADIUS = 85f
        const val CIRCLE_RADIUS_MAX = 95f
    }
}

data class Circle(
    var indexDraw: Int,
    var radius: Float,
    var displayRadius: Float = radius,
    var color: Int,
    var displayColor: Int = color
)

enum class ViewType(var color: Int) {
    NoJunk(Color.parseColor("#10AF75")), LessJunk(Color.parseColor("#FF8638")), ManyJunk(
        Color.parseColor(
            "#EF3E1C"
        )
    )

}

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density