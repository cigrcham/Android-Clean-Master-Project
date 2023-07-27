package com.phonecleaner.storagecleaner.cache.ui.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.phonecleaner.storagecleaner.cache.R

class ScanningView : View {
    private var viewRect: Rect = Rect()
    private var centerPoint: PointF = PointF()
    private var bitmapRect: RectF = RectF()
    private var gradiantRect: RectF = RectF()
    var onAnimationEnd: (() -> Unit)? = null
    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private var anim: ValueAnimator? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    var bitmap: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_phone_cleaning)
    private var animCount = 1

    fun setImage(bitmap: Bitmap) {
        this.bitmap = bitmap
        requestLayout()
    }

    fun setRepeatAnimCount(count: Int) {
        this.animCount = count
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            viewRect.set(0, 0, w, h)
            centerPoint.x = w / 2f
            centerPoint.y = h / 2f

            val bitmapWidth: Float = w / 2f
            val bitmapRatio: Float = bitmap.width.toFloat() / bitmapWidth
            val bitmapHeight: Float = bitmap.height / bitmapRatio
            // Create new bitmap and set position of bitmap
            bitmap =
                Bitmap.createScaledBitmap(bitmap, bitmapWidth.toInt(), bitmapHeight.toInt(), true)
            bitmapRect.set(0f, 0f, bitmapWidth, bitmapHeight)
            bitmapRect.offset(centerPoint.x - bitmapWidth / 2, centerPoint.y - bitmapHeight / 2)

            val margin = MARGIN.toPx
            val gradiantWidth = w / 3 * 2
            gradiantRect.set(
                centerPoint.x - gradiantWidth / 2,
                0f,
                centerPoint.x + gradiantWidth / 2,
                bitmapRect.bottom + margin
            )
            startAnimationScan()
        }
    }

    private fun startAnimationScan() {
        val margin = MARGIN.toPx
        anim =
            ValueAnimator.ofFloat(gradiantRect.bottom, bitmapRect.top - margin, gradiantRect.bottom)
                .apply {
                    duration = ANIM_DURATION
                    repeatCount = animCount
                    addUpdateListener { animation ->
                        gradiantRect.top = animation.animatedValue as Float
                        invalidate()
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            onAnimationEnd?.invoke()
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                    start()
                }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            canvas.drawBitmap(
                bitmap, centerPoint.x - bitmap.width / 2f, centerPoint.y - bitmap.height / 2, paint
            )
            paint.shader = LinearGradient(
                gradiantRect.left,
                gradiantRect.bottom,
                gradiantRect.left,
                gradiantRect.top,
                Color.parseColor("#00FFFFFF"),
                Color.parseColor("#B3FFFFFF"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(gradiantRect, 4f.toPx, 4f.toPx, paint)
        }
    }

    companion object {
        const val MARGIN = 12f
        const val ANIM_DURATION = 3000L
    }

}