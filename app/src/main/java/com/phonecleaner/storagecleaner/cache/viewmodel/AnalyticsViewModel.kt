package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.getFilenameFromPath
import com.phonecleaner.storagecleaner.cache.extension.getLongValue
import com.phonecleaner.storagecleaner.cache.extension.getStringValue
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileHelper
import com.phonecleaner.storagecleaner.cache.utils.customView.fileDup.DuplicateFinder
import com.phonecleaner.storagecleaner.cache.utils.customView.fileDup.FileFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel() {
    private val myTag: String = this::class.java.simpleName
    var listFileLiveData = MutableStateLiveData<List<FileApp>>()
    var largerFileLiveData = MutableStateLiveData<List<FileApp>>()
    val screenShotLiveData: MutableStateLiveData<List<FileApp>> = MutableStateLiveData()
    val miscFilesLiveData: MutableStateLiveData<List<FileApp>> = MutableStateLiveData()
    fun getScreenShotImages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                screenShotLiveData.postLoading()
                viewModelScope.launch(Dispatchers.IO) {
                    val projection = arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.DATE_MODIFIED,
                        MediaStore.Files.FileColumns.SIZE
                    )

                    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val listOfAllImages = ArrayList<FileApp>()
                    val selection = "${MediaStore.Images.Media.DATA} like '%/Screenshots/%'"
                    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                    val cursor: Cursor? =
                        context.contentResolver.query(uri, projection, selection, null, sortOrder)

                    val columnIndexImage =
                        cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA) ?: -1
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            if (columnIndexImage != -1) {
                                val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                                val name =
                                    cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                                        ?: path.getFilenameFromPath()
                                val modified =
                                    cursor.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                                val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                                val type = try {
                                    cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                                } catch (e: Exception) {
                                    MimeTypeMap.getSingleton()
                                        .getMimeTypeFromExtension(File(path).extension)
                                        ?: Constants.UNKNOW
                                }
                                if (!name.startsWith(".")) {
                                    listOfAllImages.add(
                                        FileApp(
                                            name = name,
                                            path = path,
                                            size = size,
                                            type = type,
                                            dateModified = modified
                                        )
                                    )
                                }
                            }
                        }
                    }
                    cursor?.close()
                    screenShotLiveData.postSuccess(listOfAllImages)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getLargerFile() {
        largerFileLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val fileList = mutableListOf<File>()
            FileHelper.getFileList(Environment.getRootDirectory(), fileList)
            FileHelper.getFileList(Environment.getExternalStorageDirectory(), fileList)
            fileList.sortByDescending { it.length() }
            largerFileLiveData.postSuccess(fileList.map { it.convertToFileApp() }.mapNotNull { it })
        }
    }

    fun getDuplicateFile() {
        listFileLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val fileFinder = FileFinder()
            val start = System.currentTimeMillis()
            fileFinder.findFiles(Environment.getExternalStorageDirectory(), Constants.MIN_BYTE)
            listFileLiveData.postSuccess(
                DuplicateFinder().findDuplicates(fileFinder.files)
                .map { it.convertToFileApp() }.mapNotNull { it })
        }
    }

    fun getMiscFiles() {
        miscFilesLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val projection = arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.SIZE
                )

                val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} != ?"
                val selectionArgs = arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString()
                )

                val cursor = context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                val listOfMiscFiles = ArrayList<FileApp>()
                cursor?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                            val name =
                                cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                                    ?: path.getFilenameFromPath()
                            val modified =
                                cursor.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                            val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                            var type = path.substringAfterLast(".", "")
                            if (type.isEmpty() || type.length > 6) {
                                type = Constants.UNKNOW
                            }
                            if (!name.startsWith(".")) {
                                listOfMiscFiles.add(
                                    FileApp(
                                        name = name,
                                        path = path,
                                        size = size,
                                        type = type,
                                        dateModified = modified
                                    )
                                )
                            }
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                    miscFilesLiveData.postSuccess(listOfMiscFiles)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                miscFilesLiveData.postError("${ex.message}")
            }
        }
    }
}