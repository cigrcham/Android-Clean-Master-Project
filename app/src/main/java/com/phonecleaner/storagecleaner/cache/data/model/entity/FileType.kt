package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep


/**
 * Created by Thinhvh on 25/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

@Keep
enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    ZIP,
    TXT,
    PPTX,
    XLSX,
    PDF,
    UNKNOWN,
}