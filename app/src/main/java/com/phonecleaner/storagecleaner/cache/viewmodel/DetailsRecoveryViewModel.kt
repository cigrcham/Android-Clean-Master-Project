package com.phonecleaner.storagecleaner.cache.viewmodel

import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.data.model.entity.RecoveryType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isDocument
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.extension.nameAndExtension
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DetailsRecoveryViewModel @Inject constructor() : BaseViewModel() {
    var restoreFileStateLiveData = MutableStateLiveData<Unit>()
    var recoverHistoryLiveData = MutableStateLiveData<ArrayList<FileApp>>()

    fun restoreFile(recoveryItem: Recovery, listSelectedFile: ArrayList<FileApp>) {
        restoreFileStateLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val folderRecoveryPath =
                root.absolutePath.plus(File.separator).plus(Constants.RECOVERY_FOLDER_NAME)
            val recoveryDirectory = File(folderRecoveryPath)
            try {
                listSelectedFile.forEach { fileApp ->
                    val oldFile = fileApp.convertToFile()
                    val newFile = File(
                        folderRecoveryPath.plus(File.separator).plus("recovery_")
                            .plus(fileApp.nameAndExtension())
                    )
                    if (!newFile.exists()) {
                        recoveryDirectory.mkdirs()
                    }
                    FileUtils.copy(oldFile, newFile)
                    oldFile.delete()
                }
                restoreFileStateLiveData.postSuccess(Unit)
            } catch (e: Exception) {
                restoreFileStateLiveData.postError(e.message)
            }
        }
    }

    fun getRecoverHistory(recoveryItem: Recovery) {
        recoverHistoryLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folderRecoveryPath =
                    root.absolutePath.plus(File.separator).plus(Constants.RECOVERY_FOLDER_NAME)
                val recoveryDirectory = File(folderRecoveryPath)
                when (recoveryItem.type) {
                    is RecoveryType.Image -> {
                        recoverHistoryLiveData.postSuccess(getHistoryImage(recoveryDirectory))
                    }
                    is RecoveryType.Video -> {
                        recoverHistoryLiveData.postSuccess(getHistoryVideo(recoveryDirectory))
                    }
                    is RecoveryType.Audio -> {
                        recoverHistoryLiveData.postSuccess(getHistoryAudio(recoveryDirectory))
                    }
                    is RecoveryType.Document -> {
                        recoverHistoryLiveData.postSuccess(getHistoryDocument(recoveryDirectory))
                    }
                    is RecoveryType.Zip -> {
                        recoverHistoryLiveData.postSuccess(getHistoryZip(recoveryDirectory))
                    }
                }
            } catch (e: Exception) {
                recoverHistoryLiveData.postError(e.message)
            }
        }
    }

    private fun getHistoryZip(recoveryDirectory: File): ArrayList<FileApp> {
        val listFile = ArrayList<FileApp>()
        recoveryDirectory.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let {
                if (it.isZip()){
                    listFile.add(it)
                }
            }
        }
        return listFile
    }

    private fun getHistoryAudio(recoveryDirectory: File): java.util.ArrayList<FileApp> {
        val listFile = ArrayList<FileApp>()
        recoveryDirectory.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let {
                if (it.isAudio()){
                    listFile.add(it)
                }
            }
        }
        return listFile
    }

    private fun getHistoryDocument(recoveryDirectory: File): java.util.ArrayList<FileApp> {
        val listFile = ArrayList<FileApp>()
        recoveryDirectory.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let {
                if (it.isDocument()){
                    listFile.add(it)
                }
            }
        }
        return listFile
    }

    private fun getHistoryVideo(recoveryDirectory: File): java.util.ArrayList<FileApp> {
        val listFile = ArrayList<FileApp>()
        recoveryDirectory.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let {
                if (it.isVideo()){
                    listFile.add(it)
                }
            }
        }
        return listFile
    }

    private fun getHistoryImage(recoveryDirectory: File): ArrayList<FileApp> {
        val listFile = ArrayList<FileApp>()
        recoveryDirectory.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let {
                if (it.isImage()){
                    listFile.add(it)
                }
            }
        }
        return listFile
    }
}