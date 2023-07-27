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
@Entity
@Parcelize
class AppInstalled(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var packageName: String = "",
    var appName: String = "",
    var modified: Long = 0L,
    var size: Long = 0L
) : Parcelable {
    @IgnoredOnParcel
    var selected: Boolean = false
    @IgnoredOnParcel
    @Ignore
    var iconBitmap: Bitmap? = null
}