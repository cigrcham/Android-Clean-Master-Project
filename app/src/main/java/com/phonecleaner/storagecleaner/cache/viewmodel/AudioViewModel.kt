package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.getLongValue
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.CursorHelper
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonFile
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonListFileApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    BaseViewModel() {
    private val myTag: String = this::class.java.simpleName
    val listAudioLiveData = MutableStateLiveData<ArrayList<Folder>>()
    val listFileAudio = MutableStateLiveData<ArrayList<FileApp>>()

    fun getAudio() {
        listAudioLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listSongFolder: ArrayList<Folder> = ArrayList()
                val listFolderName: ArrayList<String> = ArrayList()
                val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val sel =
                    "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.IS_RINGTONE} = 0 AND ${MediaStore.Audio.Media.DURATION}> '0'"
                val cursor: Cursor? = context.contentResolver.query(uri, null, sel, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val data: String =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        // If data exists
                        if (File(data).exists()) {
                            val id: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                            val name: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                            val mineType: String = try {
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
                            } catch (e: Exception) {
                                MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(File(data).extension)
                                    ?: Constants.UNKNOW
                            }
                            val album: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                            val artist: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            val timestamp: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                            val duration: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                            val artistId =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
                            val albumId =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            val folderPath = File(data).parent ?: ""
                            val folderName = File(data).parentFile?.name ?: ""
                            val modified =
                                cursor.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                            val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)

                            // Data Song
                            val song = FileApp(
                                id = id,
                                name = name,
                                path = data,
                                type = mineType,
                                size = size,
                                dateModified = modified
                            )
                            Timber.tag(myTag).d("getAudio: %s", song.toString())
                            val parentFile: File? = File(data).parentFile
                            if (listFolderName.contains(folderName)) {
                                for (folder: Folder in listSongFolder) {
                                    folder.listFile.add(song)
                                    break
                                }
                            } else {
                                val folder = Folder(
                                    id = id,
                                    name = folderName,
                                    path = parentFile?.absolutePath ?: "",
                                    coverPath = data,
                                    listFile = arrayListOf(song)
                                )
                                listSongFolder.add(folder)
                                listFolderName.add(folderName)
                            }
                        }
                    } while (cursor.moveToNext())
                }
                cursor?.close()
                listAudioLiveData.postSuccess(listSongFolder)
            } catch (exception: Exception) {
                listAudioLiveData.postError(exception.message)
            }
        }
    }

    fun getFileAudio() {
        listFileAudio.postLoading()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                listFileAudio.postSuccess(FileUtils.getFileAudio(Environment.getExternalStorageDirectory()))
            }
        } catch (ex: Exception) {
            listFileAudio.postError(ex.message)
        }
    }

    fun insertMediaStore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SingletonListFileApp.getInstance().listFileApp.forEach { fileApp ->
                    MediaScannerConnection.scanFile(
                        context, arrayOf(fileApp.path), arrayOf(fileApp.type)
                    ) { path, _ ->
                        val contentValues: ContentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileApp.name)
                            put(MediaStore.MediaColumns.MIME_TYPE, fileApp.type)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.MediaColumns.RELATIVE_PATH, path)
                                put(MediaStore.MediaColumns.IS_PENDING, 1)
                            } else {
                                put(MediaStore.MediaColumns.DATA, path)
                            }
                        }
                        val insertUri: Uri? = context.contentResolver.insert(
                            MediaStore.Audio.Media.getContentUri("external"), contentValues
                        )
                        Timber.tag(myTag).e("Uri file insert: $insertUri")
                        getAudio()
                    }
                }

            } catch (ex: Exception) {
                Timber.tag(myTag).e("Uri file failed: ${ex.message}")
            }
        }
    }

    fun updateMediaStore(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // The file was successfully renamed
                val resolve: ContentResolver = context.contentResolver
                // First, query the MediaStore to get the ID of the image
                val queryUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val queryProjection: Array<String> = arrayOf(MediaStore.Audio.Media._ID)
                val querySelection = "${MediaStore.Audio.Media.DATA}=?"
                val querySelectionArgs: Array<String?> =
                    arrayOf(SingletonFile.getInstance().oldFile?.absolutePath)
                val queryCursor: Cursor? = resolve.query(
                    queryUri, queryProjection, querySelection, querySelectionArgs, null
                )
                val audioId: Long? = if (queryCursor != null && queryCursor.moveToFirst()) {
                    queryCursor.getLong(queryCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                } else {
                    null
                }
                queryCursor?.close()

                // If we found the ID of the image, update of MediaStore with the new file name
                if (audioId != null) {
                    val updateUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId
                    )
                    val extension: String? = SingletonFile.getInstance().oldFile?.extension
                    val namePut: String =
                        if (extension?.isEmpty() == true) newName else "$newName.$extension"
                    val values: ContentValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, namePut)
                        put(MediaStore.Audio.Media.TITLE, namePut)
                        put(
                            MediaStore.Audio.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000
                        )
                    }
                    resolve.update(updateUri, values, null, null)
                }
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(SingletonFile.getInstance().newFile)
                    )
                )
            } catch (ex: Exception) {
                Timber.tag(myTag).e("Uri failed: ${ex.message}")
            }
        }
    }

    fun removeMediaStore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SingletonListFileApp.getInstance().listFileApp.forEach { fileApp ->
                    MediaScannerConnection.scanFile(
                        context, arrayOf(fileApp.path), arrayOf(fileApp.type)
                    ) { path: String, uri: Uri ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val row = context.contentResolver.delete(
                                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                MediaStore.Audio.Media._ID + "=" + CursorHelper.getIdFromDisplayName(
                                    context, fileApp.name
                                ),
                                null
                            )
                            Timber.tag(myTag).d("Number row delete file: $row")
                            Timber.tag(myTag).d("Uri delete file: $uri")
                            Timber.tag(myTag).d("Path delete file: $path")
                            Timber.tag(myTag).d(
                                "Id delete file: ${
                                    CursorHelper.getIdFromDisplayName(
                                        context, fileApp.name
                                    )
                                }"
                            )
                        }
                        getAudio()
                    }
                }
            } catch (ex: Exception) {
                Timber.tag(myTag).e("Remove file failed: ${ex.message}")
            }
        }
    }
}