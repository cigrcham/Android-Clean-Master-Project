package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

/**
 * Created by Thinhvh on 21/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

@Keep
data class StorageInfo(
    var totalStorage: Long = 0L,
    var usedStorage: Long = 0L,
    var fileCount: Int = 0,
    var usedStoragePercent: Int = 0,
)