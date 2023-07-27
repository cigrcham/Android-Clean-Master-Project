package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recent
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.getFilenameFromPath
import com.phonecleaner.storagecleaner.cache.extension.getLongValue
import com.phonecleaner.storagecleaner.cache.extension.getStringValue
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.isOreoPlus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecentFileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {
    val recentLiveData = MutableStateLiveData<List<Recent>>()
    fun getRecentByDate(limit: Int = LIMIT) {
        recentLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val listItems: ArrayList<FileApp> = arrayListOf()
            val uri: Uri = MediaStore.Files.getContentUri("external")
            val projection: Array<String> = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )
            try {
                if (isOreoPlus()) {
                    val queryArgs: Bundle = bundleOf(
                        ContentResolver.QUERY_ARG_LIMIT to limit,
                        ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED),
                        ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_ARG_SORT_DIRECTION
                    )
                    context.contentResolver?.query(uri, projection, queryArgs, null)
                } else {
                    val sortOrder =
                        "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit"
                    context.contentResolver?.query(uri, projection, null, null, sortOrder)
                }?.use { cursor: Cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            if (listItems.size < limit) {
                                val path: String =
                                    cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                                val name: String =
                                    cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                                        ?: path.getFilenameFromPath()
                                val modified: Long =
                                    cursor.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                                val size: Long =
                                    cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                                val type: String = try {
                                    cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                                } catch (e: Exception) {
                                    MimeTypeMap.getSingleton()
                                        .getMimeTypeFromExtension(File(path).extension)
                                        ?: Constants.UNKNOW
                                }
                                if (!name.startsWith(".")) {
                                    listItems.add(
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
                        } while (cursor.moveToNext())
                    }
                }
                recentLiveData.postSuccess(listItems.groupBy { it.dateModified.convertToDate() }
                    .map { Recent(it.key, it.value) })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    companion object {
        const val LIMIT = 100
    }
}