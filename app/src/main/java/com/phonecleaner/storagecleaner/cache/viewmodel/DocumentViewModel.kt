package com.phonecleaner.storagecleaner.cache.viewmodel

import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor() : BaseViewModel() {
    var listFileDocumentLiveData = MutableStateLiveData<ArrayList<FileApp>>()

    fun getListFileDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                listFileDocumentLiveData.postSuccess(FileUtils.getFileDocument(Environment.getExternalStorageDirectory()))
            } catch (e: Exception) {
                listFileDocumentLiveData.postError(e.message)
            }
        }
    }
}