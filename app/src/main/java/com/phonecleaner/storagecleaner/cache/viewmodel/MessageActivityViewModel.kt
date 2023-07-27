package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import com.phonecleaner.storagecleaner.cache.data.model.response.MessageNotificationState
import com.phonecleaner.storagecleaner.cache.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageActivityViewModel @Inject constructor(
    private val repository: Repository, @ApplicationContext private val context: Context
) : ViewModel() {
    private val myTag: String = this::class.java.simpleName
    private val _messageNotification: MutableStateFlow<MessageNotificationState> =
        MutableStateFlow(MessageNotificationState.Success(emptyList()))
    val listMessageNotification: StateFlow<MessageNotificationState> = _messageNotification
    fun getAllFile() {
        viewModelScope.launch(Dispatchers.IO) {
            _messageNotification.value = MessageNotificationState.Success(emptyList())
            repository.getAllMessage().catch { exception ->
                emit(listOf())
            }.collect {
                _messageNotification.value = MessageNotificationState.Success(it)
            }
        }
    }

    fun cancelNotification(list: List<MessageNotifi>) {
        viewModelScope.launch(Dispatchers.IO) {}
    }
}