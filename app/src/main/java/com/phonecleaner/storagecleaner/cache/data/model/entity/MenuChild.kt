package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep

@Keep
data class MenuChild(
    var name: String = "",
    var isShowSwitchCompat: Boolean = false,
    var isShowLanguage: Boolean = false
)
