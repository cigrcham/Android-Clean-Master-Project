package com.phonecleaner.storagecleaner.cache.ui.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled

class CpuCoolerView : View {
    // Rect
    private val viewRect = Rect()
    private val centerPoint: PointF = PointF()
    private var roundRectF: RectF = RectF()

    // Paint
    private var fillPaint: Paint = Paint()
    private var strokePaint: Paint = Paint()
    private var appPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }
    var bitmapSnow: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_snow)
    var onAnimationEnd: (() -> Unit)? = null
    var onProgress: ((Int, Int) -> Unit)? = null

    // Anim
    private var progressAnim: ValueAnimator? = null
    private var iconAnim: ValueAnimator? = null

    // Data
    private var listSnow: MutableList<Snow> = mutableListOf()
    private var listIconData: ArrayList<Icon> = arrayListOf()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Set top left is origin point
            viewRect.set(0, 0, w, h)
            // Set center point
            centerPoint.x = w / 2f
            centerPoint.y = h / 2f
            // Set position roundRect
            roundRectF = RectF().apply {
                left = centerPoint.x - CIRCLE_RADIUS.toPx / 2
                right = centerPoint.x + CIRCLE_RADIUS.toPx / 2
                top = centerPoint.y - CIRCLE_RADIUS.toPx / 4 - ROUND_HEIGHT.toPx
                bottom = centerPoint.y - CIRCLE_RADIUS.toPx / 3
            }
            fillPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                shader = android.graphics.LinearGradient(
                    0f,
                    0f,
                    0f,
                    height.toFloat(),
                    Color.parseColor("#445990"),
                    Color.parseColor("#141517"),
                    Shader.TileMode.MIRROR
                )
            }
            strokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = Color.parseColor("#00AEEE")
                strokeWidth = STROKE_WIDTH.toPx
            }
            initListSnow()
            if (w > 0 && h > 0) {
                startAnim()
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { it: Canvas ->
            it.drawCircle(centerPoint.x, centerPoint.y, CIRCLE_RADIUS.toPx, strokePaint)
            it.drawRoundRect(
                roundRectF, LINE_RADIUS.toPx / 2, LINE_RADIUS.toPx / 2, strokePaint
            )
            it.drawCircle(
                centerPoint.x, centerPoint.y, CIRCLE_RADIUS.toPx, fillPaint
            )
            it.drawRoundRect(
                roundRectF, LINE_RADIUS.toPx / 2, LINE_RADIUS.toPx / 2, fillPaint
            )

            listSnow.forEach {
                canvas.save()
                canvas.scale(it.scale, it.scale)
                canvas.rotate(it.rotation, it.startX.toFloat(), it.startY.toFloat())
                canvas.drawBitmap(
                    bitmapSnow, it.startX.toFloat(), it.startY.toFloat(), appPaint
                )
                canvas.restore()
            }

            listIconData.forEach {
                canvas.drawBitmap(it.icon, null, it.rect, appPaint)
            }
        }
    }

    private fun startAnimationAppIcon() {
        iconAnim = ValueAnimator.ofFloat(0f, 1f)
        iconAnim?.apply {
            addUpdateListener {
                if (listIconData.isNotEmpty()) {
                    listIconData.forEachIndexed { index, snow ->
                        snow.rect.left += 20
                        snow.rect.right += 20
                        if (snow.rect.left > snow.rect.right) {
                            onProgress?.invoke(index + 1, listIconData.size)
                        }
                    }
                }
                if (listIconData.last().rect.left > viewRect.right) {
                    onAnimationEnd?.invoke()
                    cancel()
                }
                invalidate()
            }
            repeatCount = ValueAnimator.INFINITE
        }
        iconAnim?.start()
    }

    private fun initListSnow() {
        val random = java.util.Random()
        (0..50).forEach { _ ->
            val x = -random.nextInt(viewRect.width() / 3)
            val y = -random.nextInt(viewRect.height())
            val scale = random.nextFloat()
            val rotation = 0f
            val speedX = random.nextInt(3) + 1
            val speedY = random.nextInt(5) + 1
            listSnow.add((Snow(x, y, scale, rotation, speedX, speedY)))
        }
    }

    private fun startAnim() {
        val startY = centerPoint.y - CIRCLE_RADIUS.toPx / 3
        val stopY = centerPoint.y - CIRCLE_RADIUS.toPx / 4 - ROUND_HEIGHT.toPx
        progressAnim = ValueAnimator.ofFloat(startY, stopY, startY)
        progressAnim?.apply {
            repeatCount = ValueAnimator.INFINITE
            duration = ANIMATION_DURATION
            addUpdateListener { it ->
                val value = it.animatedValue as Float
                roundRectF.top = value
                listSnow.forEach {
                    it.startX += it.speedX
                    it.startY += it.speedY
                    it.rotation += 1
                }
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                    roundRectF.top = stopY
                }

                override fun onAnimationRepeat(animation: Animator) {
                }

            })
        }
        progressAnim?.start()
    }

    fun stopAnim() {
        progressAnim?.cancel()
    }

    fun showListApp(list: ArrayList<AppInstalled>) {
        list.forEachIndexed { index, appInfo ->
            listIconData.add(getIconFromAppInfo(index, appInfo))
        }
        startAnimationAppIcon()
    }

    private fun getIconFromAppInfo(ind: Int, appInfo: AppInstalled): Icon {
        val iconSize = ICON_SIZE.toPx
        val iconPadding = ICON_PADDING.toPx
        return Icon(
            RectF(
                viewRect.left - (iconPadding * ind),
                centerPoint.y + CIRCLE_RADIUS.toPx * 3 / 2,
                viewRect.left + iconSize - (iconPadding * ind),
                centerPoint.y + CIRCLE_RADIUS.toPx * 3 / 2 + iconSize
            ), appInfo.iconBitmap ?: bitmapSnow
        )
    }

    companion object {
        const val CIRCLE_RADIUS = 60f
        const val STROKE_WIDTH = 6f
        const val ROUND_HEIGHT: Float = CIRCLE_RADIUS * 4.5f
        const val LINE_RADIUS = 80f
        const val ANIMATION_DURATION = 3000L
        const val ICON_SIZE = 50f
        const val ICON_PADDING = 150f
    }
}

data class Snow(
    var startX: Int,
    var startY: Int,
    var scale: Float,
    var rotation: Float,
    val speedX: Int,
    val speedY: Int
)

data class Icon(
    var rect: RectF,
    var icon: Bitmap,
    var startX: Float = rect.left + icon.width / 2,
    var startY: Float = rect.top + icon.height / 2,
    var scale: Float = 1f
)

