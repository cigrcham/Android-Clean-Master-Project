package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class FileStatus {
    object COPY : FileStatus()
    object EXTRACT : FileStatus()
}