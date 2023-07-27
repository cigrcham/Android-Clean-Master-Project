package com.phonecleaner.storagecleaner.cache.data.model.entity

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recovery(
    val name: String,
    val type: RecoveryType,
    val fileCount: Int = 0,
    val listFile: ArrayList<FileApp>
) : Parcelable

@Parcelize
sealed class RecoveryType : Parcelable {
    class Image(val imagePath: String) : RecoveryType()
    class Video(val videoPath: String) : RecoveryType()
    class Audio(@DrawableRes val imageRes: Int, @ColorRes val colorRes: Int) : RecoveryType()
    class Zip(@DrawableRes val imageRes: Int, @ColorRes val colorRes: Int) : RecoveryType()
    class Document(@DrawableRes val imageRes: Int, @ColorRes val colorRes: Int) : RecoveryType()
}