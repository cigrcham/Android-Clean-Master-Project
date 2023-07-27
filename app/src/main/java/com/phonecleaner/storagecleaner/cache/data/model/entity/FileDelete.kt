package com.phonecleaner.storagecleaner.cache.data.model.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "file delete")
@Parcelize
data class FileDelete(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String = "",
    var originPath: String = "",
    var currentPath: String = "",
    var type: String = "",
    var size: Long = 0,
    var dateModified: Long = 0,
) : Parcelable {
    @Ignore
    var isSelected: Boolean = false
}
