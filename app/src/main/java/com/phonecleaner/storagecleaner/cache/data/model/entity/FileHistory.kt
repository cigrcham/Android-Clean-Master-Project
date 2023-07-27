package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "file history")
data class FileHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String = "",
)
