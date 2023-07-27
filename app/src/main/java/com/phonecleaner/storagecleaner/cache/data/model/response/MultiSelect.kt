package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class MultiSelect {
    object SelectAll : MultiSelect()
    object ClearAll : MultiSelect()
    object Nothing : MultiSelect()
}
