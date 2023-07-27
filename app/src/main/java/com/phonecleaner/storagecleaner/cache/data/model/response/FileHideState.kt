package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide

@Keep
sealed class FileHideState {
    data class Success(val listFile: List<FileHide>) : FileHideState()
    data class Error(val exception: Throwable) : FileHideState()
}