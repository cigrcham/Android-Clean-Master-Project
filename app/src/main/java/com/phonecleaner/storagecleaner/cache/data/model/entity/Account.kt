package com.phonecleaner.storagecleaner.cache.data.model.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "account")
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String = "",
    var email: String = "",
    var type: String = ""
) {
    @Ignore
    var isSelected: Boolean = false
}