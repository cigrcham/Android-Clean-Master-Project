package com.phonecleaner.storagecleaner.cache.extension

import java.text.SimpleDateFormat
import java.util.*

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun Long.convertToSize(): String {
    val kilobyte: Long = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024
    val terabyte = gigabyte * 1024

    return if (this in 0 until kilobyte) {
        "$this B"
    } else if (this in kilobyte until megabyte) {
        (this / kilobyte).toString() + " KB"
    } else if (this in megabyte until gigabyte) {
        (this / megabyte).toString() + " MB"
    } else if (this in gigabyte until terabyte) {
        (this / gigabyte).toString() + " GB"
    } else if (this >= terabyte) {
        (this / terabyte).toString() + " TB"
    } else {
        "$this Bytes"
    }
}

fun Int.convertToPercent(): String {
    return "$this %"
}

fun Long.convertToDate(): String {
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    return formatter.format(this)
}

fun Long.convertToSimpleDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
    return formatter.format(this)
}
