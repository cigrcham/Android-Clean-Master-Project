package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ZipFileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    var listFileZipLiveData = MutableStateLiveData<ArrayList<FileApp>>()

    fun getListFileZip() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                listFileZipLiveData.postSuccess(FileUtils.getFileZip(Environment.getExternalStorageDirectory()))
            } catch (e: Exception) {
                listFileZipLiveData.postError(e.message)
            }
        }
    }
}