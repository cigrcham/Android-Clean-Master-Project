package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class FileState {
    object START : FileState()
    data class PREPARE(var type: FileStatus, var path: String) : FileState()
    data class LOADING(var path: String) : FileState()
    object SUCCESS : FileState()
    data class ERROR(var message: String) : FileState()
}