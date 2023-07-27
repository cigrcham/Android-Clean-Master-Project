package com.phonecleaner.storagecleaner.cache.extension

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.utils.Constants

fun RecyclerView.onLoadMore(callback: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + Constants.VISIBLE_THRESHOLD >= (recyclerView.layoutManager as LinearLayoutManager).itemCount) {
                callback.invoke()
            }
        }
    })
}