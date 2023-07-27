package com.phonecleaner.storagecleaner.cache.extension

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun String.toAmountFile(): String {
    return if (this.toInt() > 1) "$this files" else "$this file"
}

fun String.toList(): List<String> {
    val list = this.split(" ")
    val result = mutableListOf<String>()
    if (list.isNotEmpty()) {
        for (str in list) {
            if (str.isNotEmpty()) {
                result.add(str)
            }
        }
    }
    return result
}

fun String.isJPEG(): Boolean {
    if (this in listOf("jfif", "jfif-tbnl", "jpe", "jpeg", "jpg")) return true
    return false
}

fun String.getExtensionFile(): String {
    return this.substring(this.lastIndexOf(".") + 1, this.length)
}

fun String.getAppNameFromPkgName(context: Context): String {
    try {
        val packageManager: PackageManager = context.getPackageManager()
        val info: ApplicationInfo =
            packageManager.getApplicationInfo(this, PackageManager.GET_META_DATA)
        return packageManager.getApplicationLabel(info).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return ""
    }
}