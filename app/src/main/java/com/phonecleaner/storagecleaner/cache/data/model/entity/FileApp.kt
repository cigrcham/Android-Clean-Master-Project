package com.phonecleaner.storagecleaner.cache.data.model.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "file")
@Parcelize
data class FileApp(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var path: String = "",
    var type: String = "",
    var size: Long = 0,
    var dateModified: Long = 0,
    var favorite: Boolean = false
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var isSelected: Boolean = false

    @IgnoredOnParcel
    @Ignore
    var iconBitmap: Bitmap? = null
}
