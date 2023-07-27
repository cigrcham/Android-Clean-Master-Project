package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

@Keep
data class MenuGroup(
    var thumbnail: Int = -1,
    var name: String = "",
    var isExpanded: Boolean = false,
    var isDivider: Boolean = false
)
