package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class MediaStoreState {
    object NOTHING : MediaStoreState()
    object INSERT : MediaStoreState()
    object UPDATE : MediaStoreState()
    object DELETE : MediaStoreState()
}