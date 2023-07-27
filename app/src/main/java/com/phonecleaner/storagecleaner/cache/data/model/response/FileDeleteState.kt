package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete

@Keep
sealed class FileDeleteState {
    data class Success(val listFile: List<FileDelete>) : FileDeleteState()
    data class Error(val exception: Throwable) : FileDeleteState()
}