package com.phonecleaner.storagecleaner.cache.viewmodel

import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ListFileFromFolderViewModel : BaseViewModel() {

    var listFileLiveData = MutableStateLiveData<ArrayList<FileApp>>()

    fun getListFile(path: String, type: PagerType) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listFile = arrayListOf<FileApp>()
                File(path).listFiles()?.let { list ->
                    list.forEach { file ->
                        val fileApp = file.convertToFileApp()
                        when (type) {
                            PagerType.IMAGE -> {
                                if (fileApp?.isImage() == true) {
                                    listFile.add(fileApp)
                                }
                            }

                            PagerType.MUSIC -> {
                                if (fileApp?.isAudio() == true) {
                                    listFile.add(fileApp)
                                }
                            }

                            PagerType.VIDEO -> {
                                if (fileApp?.isVideo() == true) {
                                    listFile.add(fileApp)
                                }
                            }

                            else -> {}
                        }
                    }
                }
                listFileLiveData.postSuccess(listFile)
            } catch (e: Exception) {
                listFileLiveData.postError(e.message)
            }
        }
    }
}