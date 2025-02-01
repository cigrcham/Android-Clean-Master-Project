package com.phonecleaner.storagecleaner.cache.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.phonecleaner.storagecleaner.cache.R

class RoundedImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    val path = Path()
    var cornerRadius: Float = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.RoundedImageView) {
            cornerRadius = getDimension(R.styleable.RoundedImageView_cornerRadius, 0F)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val corners = floatArrayOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius
        )

        path.addRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(), corners, Path.Direction.CW
        )

        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}