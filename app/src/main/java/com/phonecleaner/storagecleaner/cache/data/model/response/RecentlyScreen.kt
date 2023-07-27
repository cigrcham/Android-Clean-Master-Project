package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class RecentlyScreen {
    object Main : RecentlyScreen()
    object ByDate : RecentlyScreen()
}
