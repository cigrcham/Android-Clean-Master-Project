package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.getFilenameFromPath
import com.phonecleaner.storagecleaner.cache.extension.getLongValue
import com.phonecleaner.storagecleaner.cache.extension.getStringValue
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.CursorHelper
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
class VideoViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    BaseViewModel() {
    private val myTag: String = this::class.java.simpleName
    var listVideoLiveData = MutableStateLiveData<ArrayList<FileApp>>()
    var listAlbumLiveData = MutableStateLiveData<ArrayList<Folder>>()

    fun getVideo() {
        listVideoLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )

            val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val listOfAllImages = ArrayList<FileApp>()

            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

            val columnIndexImage = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA) ?: -1
            cursor?.let { cur ->
                while (cur.moveToNext()) {
                    if (columnIndexImage != -1) {
                        val path = cur.getStringValue(MediaStore.Files.FileColumns.DATA)
                        val name = cur.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                            ?: path.getFilenameFromPath()
                        val modified =
                            cur.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                        val size = cur.getLongValue(MediaStore.Files.FileColumns.SIZE)
                        val type = try {
                            cur.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                        } catch (e: Exception) {
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(File(path).extension) ?: Constants.UNKNOW
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
                cur.close()
            }
            listVideoLiveData.postSuccess(listOfAllImages)
        }

    }

    fun getVideoFolder() {
        try {
            listAlbumLiveData.postLoading()
            viewModelScope.launch(Dispatchers.IO) {
                val phoneAlbums: ArrayList<Folder> = ArrayList()
                val albumsNames: ArrayList<String> = ArrayList()

                val projection = arrayOf(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.SIZE
                )
                val images = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                val cur = context.contentResolver.query(
                    images, projection, null, null, null
                )

                if (cur != null) {
                    if (cur.moveToFirst()) {
                        do {
                            val bucketName =
                                cur.getStringValue(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                            val photoUri = cur.getStringValue(MediaStore.Video.Media.DATA)

                            val path = cur.getStringValue(MediaStore.Files.FileColumns.DATA)
                            val name = cur.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                                ?: path.getFilenameFromPath()
                            val modified =
                                cur.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000
                            val size = cur.getLongValue(MediaStore.Files.FileColumns.SIZE)
                            val type = try {
                                cur.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                            } catch (e: Exception) {
                                MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(File(path).extension)
                                    ?: Constants.UNKNOW
                            }
                            val imageId = cur.getStringValue(MediaStore.Images.Media._ID)
                            val fileApp = FileApp(
                                name = name,
                                path = path,
                                size = size,
                                type = type,
                                dateModified = modified
                            )
                            val parentFile = File(path).parentFile

                            if (albumsNames.contains(bucketName)) {
                                for (album in phoneAlbums) {
                                    if (album.name == bucketName) {
                                        album.listFile.add(fileApp)
                                        break
                                    }
                                }
                            } else {
                                val album = Folder(
                                    id = imageId.toLong(),
                                    name = bucketName,
                                    path = parentFile?.absolutePath ?: "",
                                    coverPath = fileApp.path,
                                    listFile = arrayListOf(fileApp)
                                )
                                phoneAlbums.add(album)
                                albumsNames.add(bucketName)
                            }
                        } while (cur.moveToNext())
                    }
                }
                cur?.close()
                Timber.d("Thinhvh getAlbumImage: ${phoneAlbums.size}")
                listAlbumLiveData.postSuccess(phoneAlbums)
            }
        } catch (ex: Exception) {
            listAlbumLiveData.postError(null)
        }

    }

    fun insertMediaStore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SingletonListFileApp.getInstance().listFileApp.forEach { fileApp ->
                    MediaScannerConnection.scanFile(
                        context, arrayOf(fileApp.path), arrayOf(fileApp.type)
                    ) { path, uri ->
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileApp.name)
                            put(MediaStore.MediaColumns.MIME_TYPE, fileApp.type)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.MediaColumns.RELATIVE_PATH, path)
                                put(MediaStore.MediaColumns.IS_PENDING, 1)
                            } else {
                                put(MediaStore.MediaColumns.DATA, path)
                            }
                        }
                        val insertUri = context.contentResolver.insert(
                            MediaStore.Video.Media.getContentUri("external"), contentValues
                        )
                        Timber.tag(myTag).e("uri file insert: $insertUri")
                        getVideo()
                        getVideoFolder()
                    }
                }
            } catch (ex: Exception) {
                Timber.tag(myTag).e("insert file failed: ${ex.message}")
            }
        }
    }

    fun updateMediaStore(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
//                MediaScannerConnection.scanFile(
//                    context,
//                    arrayOf(SingletonFile.getInstance().newFile?.absolutePath),
//                    arrayOf(SingletonFile.getInstance().newFile?.convertToFileApp()?.type)
//                ) { path: String, uri: Uri ->
//                    val values = ContentValues()
//                    values.put(
//                        MediaStore.Images.Media.DISPLAY_NAME,
//                        SingletonFile.getInstance().newFile?.name
//                    )
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                        val row = context.contentResolver.delete(
//                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                            MediaStore.Video.Media.DATA + "=?",
//                            arrayOf(SingletonFile.getInstance().oldFile?.absolutePath)
//                        )
//                        Timber.tag(myTag).d("number row update file: $row")
//                        getVideo()
//                        getVideoFolder()
//                    } else {
//                        if (SingletonFile.getInstance().newFile?.parentFile?.name in listOf(
//                                "DCIM",
//                                "Movies",
//                                "Pictures"
//                            )
//                        ) {
//                            val insertUri: Uri? =
//                                SingletonFile.getInstance().oldFile?.name?.let { oldName ->
//                                    CursorHelper.getIdFromDisplayName(context, oldName)?.let {
//                                        ContentUris.withAppendedId(
//                                            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
//                                            it
//                                        )
//                                    }
//                                }
//                            values.put(
//                                MediaStore.Video.Media.RELATIVE_PATH,
//                                SingletonFile.getInstance().newFile?.absolutePath
//                            )
//                            values.put(MediaStore.Video.Media.IS_PENDING, 1)
//                            val row = insertUri?.let {
//                                context.contentResolver.update(
//                                    it,
//                                    values,
//                                    null,
//                                    null
//                                )
//                            }
//                            Timber.tag(myTag).d("number row update file: $row")
//                            getVideo()
//                            getVideoFolder()
//                        } else {
//
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Timber.tag(myTag).e("uri failed: ${e.message}")
//            }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(SingletonFile.getInstance().newFile?.absolutePath),
                        arrayOf(SingletonFile.getInstance().newFile?.convertToFileApp()?.type)
                    ) { path: String, uri: Uri ->
                        val values = ContentValues()
                        values.put(
                            MediaStore.Video.Media.DISPLAY_NAME,
                            SingletonFile.getInstance().newFile?.name
                        )
                        val row = context.contentResolver.delete(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "=?",
                            arrayOf(SingletonFile.getInstance().oldFile?.absolutePath)
                        )
                    }
                } else {
                    // The file was successfully renamed
                    val resolver = context.contentResolver

                    // First, query the MediaStore to get the ID of the image
                    val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    val queryProjection = arrayOf(MediaStore.Video.Media._ID)
                    val querySelection = "${MediaStore.Video.Media.DATA}=?"
                    val querySelectionArgs =
                        arrayOf(SingletonFile.getInstance().oldFile?.absolutePath)
                    val queryCursor = resolver.query(
                        queryUri, queryProjection, querySelection, querySelectionArgs, null
                    )
                    val imageId = if (queryCursor != null && queryCursor.moveToFirst()) {
                        queryCursor.getLong(queryCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    } else {
                        null
                    }
                    queryCursor?.close()

                    // If we found the ID of the image, update the MediaStore with the new file name
                    if (imageId != null) {
                        val updateUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, imageId
                        )
                        val extension = SingletonFile.getInstance().oldFile?.extension
                        val namePut: String =
                            if (extension?.isEmpty() == true) newName else "$newName.$extension"
                        val values = ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, namePut)
                            put(MediaStore.Video.Media.TITLE, namePut)
                            put(
                                MediaStore.Video.Media.DATE_MODIFIED,
                                System.currentTimeMillis() / 1000
                            )
                        }
                        resolver.update(updateUri, values, null, null)
                    }
                    context.sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(SingletonFile.getInstance().newFile)
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.tag(myTag).e("uri failed: ${e.message}")
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
                                MediaStore.Video.Media._ID + "=" + CursorHelper.getIdFromDisplayName(
                                    context, fileApp.name
                                ),
                                null
                            )
                            Timber.tag(myTag).d("number row delete file: $row")
                            Timber.tag(myTag).d("uri delete file: $uri")
                            Timber.tag(myTag).d("path delete file: $path")
                            Timber.tag(myTag).d(
                                    "id delete file: ${
                                        CursorHelper.getIdFromDisplayName(
                                            context, fileApp.name
                                        )
                                    }"
                                )
                        }
                        getVideo()
                        getVideoFolder()
                    }
                }
            } catch (e: Exception) {
                Timber.tag(myTag).e("remove file failed: ${e.message}")
            }
        }
    }

    fun onUpdateMediaStore() {
        try {
            viewModelScope.launch(Dispatchers.IO) {

            }
//        var extension: String =
//            fileApp.absolutePath
//        extension = extension.substring(extension.lastIndexOf("."))
            val values = ContentValues(2)
//        values.put(MediaStore.Video.Media.TITLE, title)
            values.put(
                MediaStore.Video.Media.DISPLAY_NAME, SingletonFile.getInstance().newFile?.name
            )
            context.contentResolver.update(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values,
                MediaStore.MediaColumns.DATA + "=?",
                arrayOf(SingletonFile.getInstance().newFile?.absolutePath)
            )
        } catch (ex: Exception) {
            Log.d("TAG555", "onUpdateMediaStore: ${ex.message}")
        }
    }
}