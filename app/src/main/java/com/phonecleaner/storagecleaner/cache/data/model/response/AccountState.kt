package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class AccountState {
    object LOADING : AccountState()
    object SUCCESS : AccountState()
    data class ERROR(var message: String) : AccountState()
}