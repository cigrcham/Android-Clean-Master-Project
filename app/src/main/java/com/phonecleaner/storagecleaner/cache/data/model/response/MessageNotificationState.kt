package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi

@Keep
sealed class MessageNotificationState {
    data class Success(val listFile: List<MessageNotifi>) : MessageNotificationState()
    data class Error(val exception: Throwable) : MessageNotificationState()
}