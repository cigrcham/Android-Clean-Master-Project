package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp

@Keep
sealed class FileSearchState {
    object Start : FileSearchState()
    object Loading : FileSearchState()
    data class Success(val listFile: MutableList<FileApp>) : FileSearchState()
}