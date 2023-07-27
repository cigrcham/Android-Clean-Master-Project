package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

/**
 * Created by Thinhvh on 25/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

@Keep
enum class PagerType {
    INTERNAL,
    IMAGE,
    MUSIC,
    VIDEO,
    APK,
    DOCUMENT,
    ZIP,
    DOWNLOAD,
    RECENTLY
}