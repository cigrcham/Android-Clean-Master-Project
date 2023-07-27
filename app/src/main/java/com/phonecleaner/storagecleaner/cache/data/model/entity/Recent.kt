package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

/**
 * Created by Thinhvh on 23/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

@Keep
data class Recent(var date: String = "", var listFile: List<FileApp> = mutableListOf())