package com.phonecleaner.storagecleaner.cache.data.model.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.response.DataTypeConverter
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "folder")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String = "",
    var path: String = "",
    var coverPath: String = "",
    @TypeConverters(DataTypeConverter::class)
    var listFile: MutableList<FileApp> = arrayListOf(),
    var favorite: Boolean = false
) : Parcelable {
    @Ignore
    var selected: Boolean = false
}