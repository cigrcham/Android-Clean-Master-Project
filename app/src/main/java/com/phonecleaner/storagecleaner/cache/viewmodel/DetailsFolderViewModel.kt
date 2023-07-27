package com.phonecleaner.storagecleaner.cache.viewmodel

import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class DetailsFolderViewModel : BaseViewModel() {

    val allFileLiveData = MutableStateLiveData<Folder>()
    val stackFolder = MutableLiveData<Stack<FileApp>>()

    fun addFolderToStack(fileApp: FileApp) {
        viewModelScope.launch(Dispatchers.IO) {
            var value = stackFolder.value
            if (value == null) {
                value = Stack<FileApp>()
            }
            value.let { newData ->
                newData.push(fileApp)
                stackFolder.postValue(newData)
            }
        }
    }

    fun getAllFileFromFolder(folderPath: String) {
        val listFileApp = ArrayList<FileApp>()
        allFileLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = File(folderPath)
                if (root.isDirectory) {
                    root.listFiles()?.forEach { file ->
                        file?.let { it ->
                            it.convertToFileApp()?.let {
                                listFileApp.add(it)
                            }
                        }
                    }
                }
                val folder = Folder(
                    name = root.name, listFile = listFileApp, path = folderPath
                )
                allFileLiveData.postSuccess(folder)
            } catch (e: Exception) {
                allFileLiveData.postError(e.message)
            }
        }
    }

    fun popToFolder(fileApp: FileApp) {
        val itemPosition = stackFolder.value?.indexOf(fileApp) ?: 0
        if (itemPosition >= 0 && itemPosition < (stackFolder.value?.size ?: 0)) {
            val list = stackFolder.value?.toMutableList()
            val newData = Stack<FileApp>()
            list?.forEachIndexed { index, element ->
                if (index <= itemPosition) {
                    newData.add(element)
                } else {
                    return@forEachIndexed
                }
            }
            stackFolder.postValue(newData)
        }
    }

    fun pasteFolder(listFolder: List<Folder>): List<FileApp> {
        try {
            val listFileApp = arrayListOf<FileApp>()
            listFolder.forEach {
                val root = it.convertToFile()
                if (root.isDirectory) {
                    root.listFiles()?.forEach { file ->
                        file?.let { fi ->
                            fi.convertToFileApp()?.let {
                                listFileApp.add(it)
                            }
                        }
                    }
                }
            }
            return listFileApp
        } catch (ex: Exception) {
            ex.printStackTrace()
            return emptyList()
        }
    }
}

