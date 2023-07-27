package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

@Keep
data class Analytics(
    var image: String = "",
    var video: String = "",
    var music: String = "",
    var app: String = "",
    var other: String = "",
    var document: Long = 0L,
    var used: Long = 0L,
    var total: Long = 0L,
)