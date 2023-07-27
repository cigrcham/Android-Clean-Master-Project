package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.data.model.entity.RecoveryType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileRecoveryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {
    var fileRecoveryLiveData = MutableStateLiveData<ArrayList<Recovery>>()
    private val listImage: ArrayList<FileApp> = ArrayList()
    private val listVideo: ArrayList<FileApp> = ArrayList()
    private val listAudio: ArrayList<FileApp> = ArrayList()
    private val listZip: ArrayList<FileApp> = ArrayList()
    private val listDocument: ArrayList<FileApp> = ArrayList()
    fun loadFileRecovery() {
        fileRecoveryLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            resetData()
            try {
                val strArr = Environment.getExternalStorageDirectory().absolutePath
                if (FileUtils.getFileList(strArr) != null) {
                    checkFileOfDirectory(strArr, (FileUtils.getFileList(strArr)))
                }
                val itemImage = Recovery(
                    context.getString(R.string.image),
                    RecoveryType.Image(listImage.first()?.path ?: ""),
                    listImage.size,
                    listImage
                )
                val itemVideo = Recovery(
                    context.getString(R.string.video),
                    RecoveryType.Video(listVideo.first()?.path ?: ""),
                    listVideo.size,
                    listVideo
                )
                val itemAudio = Recovery(
                    context.getString(R.string.audio),
                    RecoveryType.Audio(R.drawable.ic_recovery_audio, R.color.grayF6F6F6),
                    listAudio.size,
                    listAudio
                )
                val itemZip = Recovery(
                    context.getString(R.string.zip_files),
                    RecoveryType.Zip(R.drawable.ic_recovery_zip, R.color.grayF6F6F6),
                    listZip.size,
                    listZip
                )
                val itemDocument = Recovery(
                    context.getString(R.string.document),
                    RecoveryType.Document(R.drawable.ic_recovery_document, R.color.grayF6F6F6),
                    listDocument.size,
                    listDocument
                )
                fileRecoveryLiveData.postSuccess(
                    arrayListOf(itemImage, itemVideo, itemAudio, itemDocument, itemZip)
                )

            } catch (ex: Exception) {
                fileRecoveryLiveData.postError(ex.message)
            }
        }
    }

    private fun checkFileOfDirectory(folder: String, fileList: Array<File>?) {
        if (fileList != null) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    val tempSub: String = fileList[i].path
                    checkFileOfDirectory(tempSub, FileUtils.getFileList(tempSub))
                } else {
                    if (!folder.contains(Constants.RECOVERY_FOLDER_NAME)) {
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(fileList[i].path, options)
                        // If option not decode image
                        if (!(options.outWidth == -1 || options.outHeight == -1)) {
                            val file = File(fileList[i].path)
                            val fileSize = file.length().toString().toInt()
                            if (fileSize > 40000) {
                                file.convertToFileApp()?.let {
                                    listImage.add(it)
                                }
                            }
                            // If option decode image
                        } else {
                            val fileItemPath: String = fileList[i].path
                            when {
                                fileItemPath.endsWith(".mkv") || fileItemPath.endsWith(".mp4") -> {
                                    File(fileItemPath).convertToFileApp()?.let {
                                        listVideo.add(it)
                                    }
                                }

                                fileItemPath.endsWith(".opus") || fileItemPath.endsWith(".mp3") || fileItemPath.endsWith(
                                    ".aac"
                                ) || fileItemPath.endsWith(".m4a") -> {
                                    File(fileItemPath).convertToFileApp()?.let {
                                        listAudio.add(it)
                                    }
                                }

                                fileItemPath.endsWith(".txt") || fileItemPath.endsWith(".xml") || fileItemPath.endsWith(
                                    ".json"
                                ) || fileItemPath.endsWith(".log") || fileItemPath.endsWith(".xls") || fileItemPath.endsWith(
                                    ".xlsx"
                                ) || fileItemPath.endsWith(".doc") || fileItemPath.endsWith(".ppt") || fileItemPath.endsWith(
                                    ".pptx"
                                ) || fileItemPath.endsWith(".pdf") -> {
                                    File(fileItemPath).convertToFileApp()?.let {
                                        listDocument.add(it)
                                    }
                                }

                                fileItemPath.endsWith(".jar") || fileItemPath.endsWith(".zip") || fileItemPath.endsWith(
                                    ".rar"
                                ) || fileItemPath.endsWith(".gz") -> {
                                    File(fileItemPath).convertToFileApp()?.let {
                                        listZip.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetData() {
        listImage.clear()
        listVideo.clear()
        listAudio.clear()
        listZip.clear()
        listDocument.clear()
    }
}