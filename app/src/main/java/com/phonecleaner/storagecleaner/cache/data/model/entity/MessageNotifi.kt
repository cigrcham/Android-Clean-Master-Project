package com.phonecleaner.storagecleaner.cache.data.model.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Thinhvh on 25/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

@Keep
@Entity
@Parcelize
class MessageNotifi(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var packageName: String = "",
    var content: String = "",
    var title: String = "",
    var appName: String = "",
    var modified: Long = 0L,
    var keyNotification: String = "",
    var idMessage: Int = 0
) : Parcelable {
    @Ignore
    var isSelected: Boolean = false
}