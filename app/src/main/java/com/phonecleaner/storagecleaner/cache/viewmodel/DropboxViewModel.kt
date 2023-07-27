package com.phonecleaner.storagecleaner.cache.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.core.v2.files.Metadata
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxApiWrapper
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.ListFolderApiResult
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DropboxViewModel : ViewModel() {

    private val _resultApiState: MutableStateFlow<FileState> =
        MutableStateFlow(FileState.START)
    val resultApiState: StateFlow<FileState> = _resultApiState

    private val _fileList: MutableStateFlow<MutableList<Metadata>?> = MutableStateFlow(null)
    val fileList: StateFlow<MutableList<Metadata>?> = _fileList

    fun loadData(dropboxApiWrapper: DropboxApiWrapper, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val apiResult = dropboxApiWrapper.listFolders(path)) {
                is ListFolderApiResult.Error -> {
                    _resultApiState.value = FileState.ERROR(apiResult.e.message.toString())
                }
                is ListFolderApiResult.Success -> {
                    try {
                        _fileList.value = apiResult.result.entries
                        _resultApiState.value = FileState.SUCCESS
                    } catch (e: Exception) {
                        _resultApiState.value = FileState.ERROR(e.message.toString())
                    }
                }
            }
        }
    }
}