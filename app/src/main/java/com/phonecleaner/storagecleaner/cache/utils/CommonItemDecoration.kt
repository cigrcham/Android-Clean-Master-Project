package com.phonecleaner.storagecleaner.cache.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CommonItemDecoration(var rect: Rect) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position != 0) {
            outRect.top = rect.top
        }

        if (position != parent.childCount - 1) {
            outRect.bottom = rect.bottom
        }

        outRect.left = rect.left
        outRect.right = rect.right
    }
}