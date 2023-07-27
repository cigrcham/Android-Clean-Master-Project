package com.phonecleaner.storagecleaner.cache.extension

import java.util.*

private fun <E> Stack<E>?.popToItem(fileApp: E) {
    val list = this?.toMutableList<E>()
    this?.clear()
    list?.forEachIndexed { index, element ->
        if (fileApp != element) {
            this?.add(element)
        } else {
            return
        }
    }
}

