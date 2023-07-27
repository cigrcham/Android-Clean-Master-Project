package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.JunkType
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.RegexModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.StorageInfo
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.data.model.response.AccountState
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.FileStatus
import com.phonecleaner.storagecleaner.cache.data.model.response.MediaStoreState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.RecentlyScreen
import com.phonecleaner.storagecleaner.cache.data.repository.Repository
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.convertToFileDelete
import com.phonecleaner.storagecleaner.cache.extension.convertToFileHide
import com.phonecleaner.storagecleaner.cache.extension.copyDirectory
import com.phonecleaner.storagecleaner.cache.extension.copyFile
import com.phonecleaner.storagecleaner.cache.extension.getAppNameFromPkgName
import com.phonecleaner.storagecleaner.cache.extension.getFilenameFromPath
import com.phonecleaner.storagecleaner.cache.extension.getLongValue
import com.phonecleaner.storagecleaner.cache.extension.getStringValue
import com.phonecleaner.storagecleaner.cache.extension.isCacheFolder
import com.phonecleaner.storagecleaner.cache.extension.isDirectory
import com.phonecleaner.storagecleaner.cache.extension.isPackageFolder
import com.phonecleaner.storagecleaner.cache.extension.moveDirectory
import com.phonecleaner.storagecleaner.cache.extension.moveFile
import com.phonecleaner.storagecleaner.cache.extension.moveFileRecycleBin
import com.phonecleaner.storagecleaner.cache.extension.moveFolderRecycleBin
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.CursorHelper
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import com.phonecleaner.storagecleaner.cache.utils.isOreoPlus
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonDropboxMail
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonFile
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonListFileApp
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonPath
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonRecycleBinPath
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainRepository: Repository,
) : BaseViewModel() {

    private val myTag = "MainViewModel"
    var deleteJunkFileLiveData = MutableStateLiveData<Boolean>()
    val junkFileLiveData = MutableStateLiveData<ArrayList<JunkType>>()
    val storageInfoLiveData = MutableStateLiveData<StorageInfo>()
    val recentFileLiveData = MutableStateLiveData<List<FileApp>>()
    val recentFileLiveDataFromDetail = MutableStateLiveData<List<FileApp>>()
    val favoriteFileLiveData = MutableStateLiveData<List<FileApp>>()
    val selectedFileLiveData = MutableLiveData(ArrayList<FileApp>())
    val selectedFolderLiveData = MutableLiveData(ArrayList<Folder>())
    val selectedFileDeleteLiveData = MutableLiveData(ArrayList<FileDelete>())
    val selectedFileHideLiveData = MutableLiveData(ArrayList<FileHide>())
    val selectedAppLiveData = MutableLiveData(ArrayList<AppInstalled>())
    val selectedAccountLiveData = MutableLiveData(ArrayList<Account>())
    val layoutTypeLiveData = MutableLiveData(LayoutType.LINEAR)
    val sortedByLiveData = MutableLiveData("")
    val multiLiveData = MutableLiveData<MultiSelect>(MultiSelect.Nothing)
    val eventClick = MutableLiveData<RegexModel>()

    private val _openFolderStateFlow: MutableStateFlow<FileApp?> = MutableStateFlow(null)
    val openFolderStateFlow: StateFlow<FileApp?> = _openFolderStateFlow

    private val _fileStateFlow: MutableStateFlow<FileState> = MutableStateFlow(FileState.START)
    val fileStateFlow: StateFlow<FileState> = _fileStateFlow

    private val _mediaStoreStateFlow: MutableStateFlow<MediaStoreState> =
        MutableStateFlow(MediaStoreState.NOTHING)
    val mediaStoreStateFlow: StateFlow<MediaStoreState> = _mediaStoreStateFlow

    private val _createFolderStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val createFolderStateFlow: StateFlow<Boolean> = _createFolderStateFlow

    private val _accountStateFlow: MutableStateFlow<AccountState> =
        MutableStateFlow(AccountState.LOADING)
    val accountStateFlow: StateFlow<AccountState> = _accountStateFlow

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            val listAccount = mutableListOf<Account>()
            _accountStateFlow.value = AccountState.LOADING
            try {
                selectedAccountLiveData.value?.let { list ->
                    listAccount.addAll(list)
                    list.forEach { account ->
                        mainRepository.deleteAccount(account.id)
                        listAccount.remove(account)
                        if (account.email == SingletonDropboxMail.getInstance().mail) {
                            SingletonDropboxMail.getInstance().mail = ""
                        }
                    }
                }
                _accountStateFlow.value = AccountState.SUCCESS
                selectedAccountLiveData.postValue(arrayListOf())
            } catch (ex: Exception) {
                _accountStateFlow.value = AccountState.ERROR("${ex.message}")
            }
        }
    }

    fun resetAccountState() {
        viewModelScope.launch(Dispatchers.IO) {
            _accountStateFlow.value = AccountState.LOADING
        }
    }

    fun getStorageInfo() {
        storageInfoLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)
            val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
            val internalTotal: Long = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
            val internalFree: Long =
                internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
            val externalTotal: Long = externalStatFs.blockCountLong * externalStatFs.blockSizeLong
            val externalFree: Long =
                externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong
            val total = internalTotal + externalTotal
            val free = internalFree + externalFree
            val used = total - free
            val usedPercent = (used.toFloat() / total.toFloat() * 100).toInt()
            val fileCount = FileUtils.getTotalFileCount(Environment.getExternalStorageDirectory())
            storageInfoLiveData.postSuccess(StorageInfo(total, used, fileCount, usedPercent))
        }
    }

    fun getRecentFile(limit: Int = RECENTS_LIMIT) {
        recentFileLiveData.postLoading()
        recentFileLiveData.postSuccess(listOf())
        viewModelScope.launch(Dispatchers.IO) {
            val listItems = arrayListOf<FileApp>()
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )
            try {
                if (isOreoPlus()) {
                    val queryArgs = bundleOf(
                        ContentResolver.QUERY_ARG_LIMIT to limit,
                        ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED),
                        ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                    context.contentResolver?.query(uri, projection, queryArgs, null)
                } else {
                    val sortOrder =
                        "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit"
                    context.contentResolver?.query(uri, projection, null, null, sortOrder)
                }?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            if (listItems.size < limit) {
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
                recentFileLiveData.postSuccess(listItems)
            } catch (e: Exception) {
                recentFileLiveData.postError(e.message)
            }
        }
    }

    fun getRecentFileFromDetail(limit: Int = RECENTS_LIMIT) {
        recentFileLiveDataFromDetail.postLoading()
        recentFileLiveDataFromDetail.postSuccess(listOf())
        viewModelScope.launch(Dispatchers.IO) {
            val listItems = arrayListOf<FileApp>()
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )
            try {
                if (isOreoPlus()) {
                    val queryArgs = bundleOf(
                        ContentResolver.QUERY_ARG_LIMIT to limit,
                        ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED),
                        ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                    context.contentResolver?.query(uri, projection, queryArgs, null)
                } else {
                    val sortOrder =
                        "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit"
                    context.contentResolver?.query(uri, projection, null, null, sortOrder)
                }?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            if (listItems.size < limit) {
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
                recentFileLiveDataFromDetail.postSuccess(listItems)
            } catch (e: Exception) {
                recentFileLiveDataFromDetail.postError(e.message)
            }
        }
    }

    fun insertMediaStore(recentlyScreen: RecentlyScreen) {
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
                            MediaStore.Files.getContentUri("external"), contentValues
                        )
                        Timber.tag(myTag).e("uri file insert: $insertUri")
                        when (recentlyScreen) {
                            is RecentlyScreen.Main -> {
                                getRecentFile(RECENTS_LIMIT)
                            }

                            is RecentlyScreen.ByDate -> {
                                getRecentFile(RECENTS_BY_DATE_LIMIT)
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.tag(myTag).e("insert file failed: ${ex.message}")
            }
        }
    }

    fun updateMediaStore(recentlyScreen: RecentlyScreen) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(SingletonFile.getInstance().newFile?.absolutePath),
                    arrayOf(SingletonFile.getInstance().newFile?.convertToFileApp()?.type)
                ) { path: String, uri: Uri ->
                    val values = ContentValues()
                    values.put(
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        SingletonFile.getInstance().newFile?.name
                    )
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val row = context.contentResolver.delete(
                            MediaStore.Files.getContentUri("external"),
                            MediaStore.Files.FileColumns.DATA + "=?",
                            arrayOf(SingletonFile.getInstance().oldFile?.absolutePath)
                        )
                        Timber.tag(myTag).d("number row update file: $row")
                        when (recentlyScreen) {
                            is RecentlyScreen.Main -> {
                                getRecentFile(RECENTS_LIMIT)
                            }

                            is RecentlyScreen.ByDate -> {
                                getRecentFile(RECENTS_BY_DATE_LIMIT)
                            }
                        }
                    } else {
                        if (SingletonFile.getInstance().newFile?.parentFile?.name in listOf(
                                "Alarms",
                                "DCIM",
                                "Music",
                                "Movies",
                                "Notifications",
                                "Pictures",
                                "Podcasts",
                                "Ringtones",
                            )
                        ) {
                            val insertUri: Uri? =
                                SingletonFile.getInstance().oldFile?.name?.let { oldName ->
                                    CursorHelper.getIdFromDisplayName(context, oldName)?.let {
                                        ContentUris.withAppendedId(
                                            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                            it
                                        )
                                    }
                                }
                            values.put(
                                MediaStore.Files.FileColumns.RELATIVE_PATH,
                                SingletonFile.getInstance().newFile?.absolutePath
                            )
                            values.put(MediaStore.Images.Media.IS_PENDING, 1)
                            val row = insertUri?.let {
                                context.contentResolver.update(
                                    it, values, null, null
                                )
                            }
                            Timber.tag(myTag).d("number row update file: $row")
                            when (recentlyScreen) {
                                is RecentlyScreen.Main -> {
                                    getRecentFile()
                                }

                                is RecentlyScreen.ByDate -> {
                                    getRecentFile(RECENTS_BY_DATE_LIMIT)
                                }
                            }
                        } else {
                            when (recentlyScreen) {
                                is RecentlyScreen.Main -> {
                                    getRecentFile(RECENTS_LIMIT)
                                }

                                is RecentlyScreen.ByDate -> {
                                    getRecentFile(RECENTS_BY_DATE_LIMIT)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(myTag).e("uri failed: ${e.message}")
            }
        }
    }

    fun removeMediaStore(recentlyScreen: RecentlyScreen) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SingletonListFileApp.getInstance().listFileApp.forEach { fileApp ->
                    MediaScannerConnection.scanFile(
                        context, arrayOf(fileApp.path), arrayOf(fileApp.type)
                    ) { path: String, uri: Uri ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val row = context.contentResolver.delete(
                                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                MediaStore.Images.Media._ID + "=" + CursorHelper.getIdFromDisplayName(
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
                        when (recentlyScreen) {
                            is RecentlyScreen.Main -> {
                                getRecentFile()
                            }

                            is RecentlyScreen.ByDate -> {
                                getRecentFile(RECENTS_BY_DATE_LIMIT)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(myTag).e("remove file failed: ${e.message}")
            }
        }
    }

    fun getFavoriteFile(limit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteFileLiveData.postLoading()
            val listItems = arrayListOf<FileApp>()
            if (limit) {
                for (i in mainRepository.getAllFileFavorite().indices) {
                    if (i < RECENTS_LIMIT) {
                        listItems.add(mainRepository.getAllFileFavorite()[i])
                    } else {
                        break
                    }
                }
            } else {
                listItems.addAll(mainRepository.getAllFileFavorite())
            }
            favoriteFileLiveData.postSuccess(listItems)
        }
    }

    fun onSelectFile(file: FileApp, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag(myTag).d("_fileStateFlow: ${_fileStateFlow.value}")
            if (_fileStateFlow.value != FileState.START) {
                selectedFileLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            Timber.tag(myTag).d("selectedFileLiveData: ${selectedFileLiveData.value}")
            val listFile = selectedFileLiveData.value ?: arrayListOf()
            if (file.isSelected) {
                listFile.add(file)
            } else {
                if (listFile.contains(file)) {
                    listFile.remove(file)
                }
            }
            selectedFileLiveData.postValue(listFile)
        }
    }

    fun onSelectFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_fileStateFlow.value != FileState.START) {
                selectedFolderLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            val listFolder = selectedFolderLiveData.value ?: arrayListOf()
            if (folder.selected) {
                listFolder.add(folder)
            } else {
                if (listFolder.contains(folder)) {
                    listFolder.remove(folder)
                }
            }
            selectedFolderLiveData.postValue(listFolder)
        }
    }

    fun onSelectFileDelete(file: FileDelete) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_fileStateFlow.value != FileState.START) {
                selectedFileDeleteLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            val listFile = selectedFileDeleteLiveData.value ?: arrayListOf()
            if (file.isSelected) {
                listFile.add(file)
            } else {
                if (listFile.contains(file)) {
                    listFile.remove(file)
                }
            }
            selectedFileDeleteLiveData.postValue(listFile)
        }
    }

    fun onSelectFileHide(file: FileHide) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_fileStateFlow.value != FileState.START) {
                selectedFileHideLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            val listFile = selectedFileHideLiveData.value ?: arrayListOf()
            if (file.isSelected) {
                listFile.add(file)
            } else {
                if (listFile.contains(file)) {
                    listFile.remove(file)
                }
            }
            selectedFileHideLiveData.postValue(listFile)
        }
    }

    fun onSelectApp(file: AppInstalled) {
        viewModelScope.launch(Dispatchers.IO) {
            val listFile = selectedAppLiveData.value ?: arrayListOf()
            if (_fileStateFlow.value != FileState.START) {
                selectedAppLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            if (file.selected) {
                listFile.add(file)
            } else {
                if (listFile.contains(file)) {
                    listFile.remove(file)
                } else {
                    // Check
                    listFile.removeIf { it.id == file.id && it.appName == file.appName }
                }
            }
            selectedAppLiveData.postValue(listFile)
        }
    }

    fun onSelectAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_fileStateFlow.value != FileState.START) {
                selectedAccountLiveData.postValue(arrayListOf())
                _fileStateFlow.value = FileState.START
            }
            val listAccount = selectedAccountLiveData.value ?: arrayListOf()
            if (account.isSelected) {
                listAccount.add(account)
            } else {
                if (listAccount.contains(account)) {
                    listAccount.remove(account)
                }
            }
            selectedAccountLiveData.postValue(listAccount)
        }
    }

    fun multiSelectFile(list: ArrayList<FileApp>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedFileLiveData.postValue(arrayListOf())
            selectedFileLiveData.postValue(list)
        }
    }

    fun multiSelectFolder(list: ArrayList<Folder>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedFolderLiveData.postValue(arrayListOf())
            selectedFolderLiveData.postValue(list)
        }
    }

    fun multiSelectFileDelete(list: ArrayList<FileDelete>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedFileDeleteLiveData.postValue(arrayListOf())
            selectedFileDeleteLiveData.postValue(list)
        }
    }

    fun multiSelectApp(list: ArrayList<AppInstalled>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedAppLiveData.postValue(arrayListOf())
            selectedAppLiveData.postValue(list)
        }
    }

    fun multiSelectAccount(list: ArrayList<Account>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedAccountLiveData.postValue(arrayListOf())
            selectedAccountLiveData.postValue(list)
        }
    }

    fun changeLayoutType() {
        viewModelScope.launch(Dispatchers.IO) {
            var currentType = layoutTypeLiveData.value
            if (currentType == LayoutType.LINEAR) {
                currentType = LayoutType.GRID
            } else {
                currentType = LayoutType.LINEAR
            }
            layoutTypeLiveData.postValue(currentType)
        }
    }

    fun changeLayoutLinear() {
        if (layoutTypeLiveData.value == LayoutType.GRID) layoutTypeLiveData.postValue(LayoutType.LINEAR)
    }

    fun changeLayoutGrid() {
        if (layoutTypeLiveData.value == LayoutType.LINEAR) layoutTypeLiveData.postValue(LayoutType.GRID)
    }

    fun multiSelect(optionSelect: MultiSelect) {
        viewModelScope.launch(Dispatchers.IO) {
            when (optionSelect) {
                MultiSelect.ClearAll -> {
                    Timber.d("vao set multiselect")
                    if (selectedFileLiveData.value?.isEmpty() != true || selectedAppLiveData.value?.isEmpty() != true || selectedFolderLiveData.value?.isEmpty() != true || selectedAccountLiveData.value?.isEmpty() != true) {
                        Timber.d("vao set multiselect 11111")
                        multiLiveData.postValue(optionSelect)
                    }
                }

                else -> {
                    Timber.d("vao set multiselect 2222")
                    multiLiveData.postValue(optionSelect)
                }
            }
        }
    }

    fun sortedBy(condition: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sortedByLiveData.postValue(condition)
        }
    }

    fun copyFile(
        onLoading: (() -> Unit)? = null,
        onSuccess: ((ArrayList<FileApp>) -> Unit)? = null,
        onFail: ((String?) -> Unit)? = null
    ) {
        val listPastFile = arrayListOf<FileApp>()
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                SingletonListFileApp.getInstance().listFileApp.clear()
                selectedFileLiveData.value?.let { list ->
                    listPastFile.addAll(list)
                    list.forEach { fileApp ->
                        if (fileApp.convertToFile().isDirectory) {
                            val destinationPath =
                                "${SingletonPath.getInstance().path}/${fileApp.name}"
                            fileApp.convertToFile().copyDirectory(destinationPath)
                        } else {
                            val destinationPath =
                                "${SingletonPath.getInstance().path}/${fileApp.convertToFile().nameWithoutExtension}"
                            val extension = fileApp.convertToFile().extension
                            fileApp.convertToFile().copyFile(destinationPath, extension)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke(listPastFile)
                }
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke(ex.message)
                }
            }
        }
    }

    fun copyFolder(
        onLoading: (() -> Unit)? = null,
        onSuccess: ((ArrayList<Folder>) -> Unit)? = null,
        onFail: ((String?) -> Unit)? = null
    ) {
        val listFolder = arrayListOf<Folder>()
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                selectedFolderLiveData.value?.let { list ->
                    listFolder.addAll(list)
                    list.forEach { folder ->
                        val destinationPath = "${SingletonPath.getInstance().path}/${folder.name}"
                        folder.convertToFile().copyDirectory(destinationPath)
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke(listFolder)
                }
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke(ex.message)
                }
                Timber.tag(myTag).e("copy folder failed: ${ex.message}")
            }
        }
    }

    fun moveFile(
        onLoading: (() -> Unit)? = null,
        onSuccess: ((ArrayList<FileApp>) -> Unit)? = null,
        onFail: ((String?) -> Unit)? = null
    ) {
        val listFileApp = arrayListOf<FileApp>()
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                SingletonListFileApp.getInstance().listFileApp.clear()
                selectedFileLiveData.value?.let { list ->
                    listFileApp.addAll(list)
                    list.forEach { fileApp ->
                        if (fileApp.convertToFile().isDirectory) {
                            val destinationPath =
                                "${SingletonPath.getInstance().path}/${fileApp.name}"
                            fileApp.convertToFile().copyDirectory(destinationPath)
                            fileApp.convertToFile().deleteRecursively()
                        } else {
                            val destinationPath =
                                "${SingletonPath.getInstance().path}/${fileApp.convertToFile().nameWithoutExtension}"
                            val extension = fileApp.convertToFile().extension
                            fileApp.convertToFile().moveFile(destinationPath, extension)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke(listFileApp)
                }
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke(ex.message)
                }
            }
        }
    }

    fun moveFolder(
        onLoading: (() -> Unit)? = null,
        onSuccess: ((ArrayList<Folder>) -> Unit)? = null,
        onFail: ((String?) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                selectedFolderLiveData.value?.let { list ->
                    list.forEach { folder ->
                        val destinationPath = "${SingletonPath.getInstance().path}/${folder.name}"
                        folder.convertToFile().moveDirectory(destinationPath)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess?.invoke(list)
                    }
                }
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke(ex.message)
                }
            }
        }
    }

    fun deleteFile(
        fileApp: FileApp,
        isDeletePermanently: Boolean,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                if (isDeletePermanently) {
                    if (fileApp.convertToFile().isDirectory) {
                        fileApp.convertToFile().list()?.let { list ->
                            list.forEach { filePath ->
                                File(filePath).delete()
                                File(filePath).convertToFileApp()?.let {
                                    SingletonListFileApp.getInstance().listFileApp.add(it)
                                }
                            }
                        }
                        fileApp.convertToFile().deleteRecursively()

                    } else {
                        fileApp.convertToFile().delete()
                        SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                    }
                } else {
                    SingletonListFileApp.getInstance().listFileApp.clear()
                    var count = 1
                    var destinationPath = ""
                    if (fileApp.isDirectory()) {
                        destinationPath =
                            "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}"
                        while (!File(destinationPath).mkdir()) {
                            destinationPath =
                                "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}($count)"
                            count++
                        }
                        fileApp.convertToFile().moveFolderRecycleBin(destinationPath)
                        fileApp.convertToFile().deleteRecursively()
                    } else {
                        val rootPath =
                            SingletonPath.getInstance().path.ifEmpty { fileApp.convertToFile().parentFile?.absolutePath }
                        destinationPath =
                            "${SingletonRecycleBinPath.getInstance().path}/${fileApp.convertToFile().nameWithoutExtension}.${
                                fileApp.convertToFile().extension
                            }"
                        while (!File(destinationPath).createNewFile()) {
                            destinationPath =
                                "${rootPath}/${fileApp.convertToFile().nameWithoutExtension}($count).${
                                    fileApp.convertToFile().extension
                                }"
                            count++
                        }
                        fileApp.convertToFile().moveFileRecycleBin(destinationPath)
                        fileApp.convertToFile().delete()
                        if (fileApp.favorite) {
                            mainRepository.deleteFileFavorite(fileApp.id)
                        }
                        fileApp.convertToFileDelete(destinationPath)
                            ?.let { mainRepository.insertFileDelete(it) }
                        SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke()
                }
                _mediaStoreStateFlow.value = MediaStoreState.DELETE
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke()
                }
            }
        }
    }

    fun deleteFile(
        isDeletePermanently: Boolean,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                if (isDeletePermanently) {
                    selectedFileLiveData.value?.let { list ->
                        list.forEach { fileApp ->
                            if (fileApp.convertToFile().isDirectory) {
                                fileApp.convertToFile().list()?.let { list ->
                                    list.forEach { filePath ->
                                        File(filePath).delete()
                                        File(filePath).convertToFileApp()?.let {
                                            SingletonListFileApp.getInstance().listFileApp.add(it)
                                        }
                                    }
                                }
                                fileApp.convertToFile().deleteRecursively()
                            } else {
                                fileApp.convertToFile().delete()
                                SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                            }
                        }
                    }
                } else {
                    SingletonListFileApp.getInstance().listFileApp.clear()
                    selectedFileLiveData.value?.let { list ->
                        list.forEach { fileApp ->
                            var count = 1
                            var destinationPath = ""
                            if (fileApp.isDirectory()) {
                                destinationPath =
                                    "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}"
                                while (!File(destinationPath).mkdir()) {
                                    destinationPath =
                                        "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}($count)"
                                    count++
                                }
                                fileApp.convertToFile().moveFolderRecycleBin(destinationPath)
                                fileApp.convertToFile().deleteRecursively()
                            } else {
                                val rootPath =
                                    SingletonPath.getInstance().path.ifEmpty { fileApp.convertToFile().parentFile?.absolutePath }
                                destinationPath =
                                    "${SingletonRecycleBinPath.getInstance().path}/${fileApp.convertToFile().nameWithoutExtension}.${
                                        fileApp.convertToFile().extension
                                    }"
                                while (!File(destinationPath).createNewFile()) {
                                    destinationPath =
                                        "${rootPath}/${fileApp.convertToFile().nameWithoutExtension}($count).${
                                            fileApp.convertToFile().extension
                                        }"
                                    count++
                                }
                                fileApp.convertToFile().moveFileRecycleBin(destinationPath)
                                fileApp.convertToFile().delete()
                                if (fileApp.favorite) {
                                    mainRepository.deleteFileFavorite(fileApp.id)
                                }
                            }
                            fileApp.convertToFileDelete(destinationPath)
                                ?.let { mainRepository.insertFileDelete(it) }
                            SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke()
                }
                _mediaStoreStateFlow.value = MediaStoreState.DELETE
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke()
                }
            }
        }
    }

    fun deleteFolder(
        isDeletePermanently: Boolean,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            try {
                SingletonListFileApp.getInstance().listFileApp.clear()
                if (isDeletePermanently) {
                    selectedFolderLiveData.value?.let { folers ->
                        folers.forEach { folder ->
                            folder.convertToFile().deleteRecursively()
                        }
                    }
                } else {
                    selectedFolderLiveData.value?.let { list ->
                        list.forEach { folders ->
                            folders.listFile.forEach { fileApp ->
                                var count = 1
                                var destinationPath = ""
                                if (fileApp.convertToFile().isDirectory) {
                                    destinationPath =
                                        "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}"
                                    while (!File(destinationPath).mkdir()) {
                                        destinationPath =
                                            "${SingletonRecycleBinPath.getInstance().path}/${fileApp.name}($count)"
                                        count++
                                    }
                                    fileApp.convertToFile().moveFolderRecycleBin(destinationPath)
                                    fileApp.convertToFileDelete(destinationPath)
                                        ?.let { mainRepository.insertFileDelete(it) }
                                    fileApp.convertToFile().deleteRecursively()
                                } else {
                                    val rootPath =
                                        SingletonPath.getInstance().path.ifEmpty { fileApp.convertToFile().parentFile?.absolutePath }
                                    destinationPath =
                                        "${rootPath}/${fileApp.convertToFile().nameWithoutExtension}.${fileApp.convertToFile().extension}"
                                    while (!File(destinationPath).createNewFile()) {
                                        destinationPath =
                                            "${rootPath}/${fileApp.convertToFile().nameWithoutExtension}($count).${fileApp.convertToFile().extension}"
                                        count++
                                    }
                                    fileApp.convertToFile().moveFileRecycleBin(destinationPath)
                                    fileApp.convertToFileDelete(destinationPath)
                                        ?.let { mainRepository.insertFileDelete(it) }
                                    fileApp.convertToFile().delete()
                                }
                                SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                            }
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess?.invoke()
                }
                _mediaStoreStateFlow.value = MediaStoreState.DELETE
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFail?.invoke()
                }
            }
        }
    }

    fun deletePermanentlyFile(onAccountDelete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _fileStateFlow.value = FileState.LOADING(Constants.DELETE)
            try {
                selectedFileDeleteLiveData.value?.let { list ->
                    list.forEach { file ->
                        File(file.currentPath).delete()
                        mainRepository.deleteFileDelete(file.id)
                    }
                }
                selectedAccountLiveData.value?.let { list ->
                    list.forEach { account ->
                        mainRepository.deleteAccount(account.id)
                        if (account.email == SingletonDropboxMail.getInstance().mail) {
                            SingletonDropboxMail.getInstance().mail = ""
                        }
                    }
                }
                onAccountDelete?.invoke()
                _fileStateFlow.value = FileState.SUCCESS
                _mediaStoreStateFlow.value = MediaStoreState.DELETE
            } catch (ex: Exception) {
                _fileStateFlow.value = FileState.ERROR("${ex.message}")
            }
        }
    }

    fun restockFile() {
        viewModelScope.launch(Dispatchers.IO) {
            _fileStateFlow.value = FileState.LOADING(Constants.RESTOCK)
            try {
                selectedFileDeleteLiveData.value?.let { list ->
                    list.forEach { fileDelete ->
                        var destinationPath = fileDelete.originPath
                        var count = 1
                        if (File(fileDelete.currentPath).isDirectory) {
                            while (!File(destinationPath).mkdir()) {
                                destinationPath = "${fileDelete.originPath}($count)"
                                count++
                            }
                            File(fileDelete.currentPath).moveFolderRecycleBin(destinationPath)
                            File(fileDelete.currentPath).deleteRecursively()
                        } else {
                            while (!File(fileDelete.originPath).createNewFile()) {
                                destinationPath =
                                    "${File(fileDelete.originPath).parentFile?.path}/${
                                        File(fileDelete.originPath).nameWithoutExtension
                                    }($count).${File(fileDelete.originPath).extension}"
                                count++
                            }
                            File(fileDelete.currentPath).moveFileRecycleBin(destinationPath)
                            File(fileDelete.currentPath).delete()
                        }
                        mainRepository.deleteFileDelete(fileDelete.id)
                    }
                }
                _fileStateFlow.value = FileState.SUCCESS
                _mediaStoreStateFlow.value = MediaStoreState.INSERT
            } catch (ex: Exception) {
                _fileStateFlow.value = FileState.ERROR("${ex.message}")
            }
        }
    }

    fun setName(
        newName: String,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            selectedFileLiveData.value?.let { fileApps ->
                if (fileApps.isNotEmpty()) {
                    try {
                        val extension = fileApps[0].convertToFile().extension
                        val name = if (extension.isEmpty()) {
                            "${fileApps[0].convertToFile().parentFile?.path}/$newName"
                        } else {
                            "${fileApps[0].convertToFile().parentFile?.path}/$newName.$extension"
                        }
                        fileApps[0].convertToFile().renameTo(File(name))
                        SingletonFile.getInstance().newFile = File(name)
                        SingletonFile.getInstance().oldFile = fileApps[0].convertToFile()
                        if (fileApps[0].favorite) {
                            val fileFavorite = fileApps[0]
                            fileFavorite.name = "$newName.$extension"
                            fileFavorite.path = SingletonFile.getInstance().newFile?.path.toString()
                            fileFavorite.dateModified =
                                SingletonFile.getInstance().newFile?.lastModified()!!
                            mainRepository.updateFileFavorite(fileFavorite)
                        }
                        delay(1000)
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess?.invoke()
                        }
                    } catch (ex: Exception) {
                        viewModelScope.launch(Dispatchers.Main) {
                            onFail?.invoke()
                        }
                    }
                }
            }

            selectedFolderLiveData.value?.let { folders ->
                if (folders.isNotEmpty()) {
                    try {
                        val path = folders[0].path
                        val name = "${File(path).parentFile?.absolutePath}/$newName"
                        File(path).renameTo(File(name))
                        SingletonFile.getInstance().newFile = File(name)
                        SingletonFile.getInstance().oldFile = File(path)
                        _fileStateFlow.value = FileState.SUCCESS
                        _mediaStoreStateFlow.value = MediaStoreState.UPDATE
                        onSuccess?.invoke()
                    } catch (ex: Exception) {
                        onFail?.invoke()
                        _fileStateFlow.value = FileState.ERROR("${ex.message}")
                    }
                }
            }

            selectedAccountLiveData.value?.let { list ->
                try {
                    if (list.isNotEmpty()) {
                        val account = list[0]
                        account.name = newName
                        mainRepository.renameAccount(account)
                        onSuccess?.invoke()
                    }
                } catch (e: Exception) {
                    onFail?.invoke()
                }
            }

        }
    }

    fun openDirectory(fileApp: FileApp) {
        viewModelScope.launch(Dispatchers.IO) {
            _openFolderStateFlow.value = fileApp
        }
    }

    fun closeDirectory() {
        viewModelScope.launch(Dispatchers.IO) {
            _openFolderStateFlow.value = null
        }
    }

    fun handleFileFavorite(
        fileApp: FileApp,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onDeleteSuccess: (() -> Unit)?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            if (!fileApp.isDirectory()) {
                if (fileApp.favorite || mainRepository.getItemFavorite(fileApp.id)) {
                    mainRepository.deleteFileFavorite(fileApp.id)
                    viewModelScope.launch(Dispatchers.Main) {
                        onDeleteSuccess?.invoke()
                    }
                } else {
                    val file = FileApp(
                        id = fileApp.id,
                        name = fileApp.name,
                        path = fileApp.path,
                        type = fileApp.type,
                        size = fileApp.size,
                        dateModified = fileApp.dateModified,
                        favorite = true
                    )
                    mainRepository.insertFileFavorite(file)
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess?.invoke()
                    }
                }
            }
        }

    }

    fun handleFileFavorite(
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onDeleteSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) {
                onLoading?.invoke()
            }
            selectedFileLiveData.value?.let { fileApps ->
                fileApps.forEach { fileApp ->
                    if (!fileApp.isDirectory()) {
                        if (fileApp.favorite || mainRepository.getItemFavorite(fileApp.id)) {
                            mainRepository.deleteFileFavorite(fileApp.id)
                            viewModelScope.launch(Dispatchers.Main) {
                                onDeleteSuccess?.invoke()
                            }
                        } else {
                            val file = FileApp(
                                id = fileApp.id,
                                name = fileApp.name,
                                path = fileApp.path,
                                type = fileApp.type,
                                size = fileApp.size,
                                dateModified = fileApp.dateModified,
                                favorite = true
                            )
                            mainRepository.insertFileFavorite(file)
                            viewModelScope.launch(Dispatchers.Main) {
                                onSuccess?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }

    fun hideFile(
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        try {
            SingletonListFileApp.getInstance().listFileApp.clear()
            viewModelScope.launch(Dispatchers.IO) {
                val pathHide =
                    context.getDir(context.getString(R.string.file_hide), Context.MODE_PRIVATE)
                val dirPath = File("$pathHide${File.pathSeparator}Hide")
                if (!dirPath.exists()) {
                    dirPath.mkdir()
                }
                selectedFileLiveData.value?.let { list ->
                    list.forEach { fileApp ->
                        var count = 1
                        var destinationPath = ""
                        if (fileApp.isDirectory()) {
                            destinationPath = "${dirPath}/${fileApp.name}"
                            while (!File(destinationPath).mkdir()) {
                                destinationPath = "${dirPath}/${fileApp.name}($count)"
                                count++
                            }
                            Log.d("TAG222", "hideFile: ${destinationPath}")
                            fileApp.convertToFile().moveFolderRecycleBin(destinationPath)
                            fileApp.convertToFile().deleteRecursively()
                            viewModelScope.launch(Dispatchers.Main) {
                                onSuccess?.invoke()
                            }
                        } else {
                            destinationPath =
                                "${dirPath}/${fileApp.convertToFile().nameWithoutExtension}.${
                                    fileApp.convertToFile().extension
                                }"
                            while (!File(destinationPath).createNewFile()) {
                                destinationPath =
                                    "${dirPath}/${fileApp.convertToFile().nameWithoutExtension}($count).${
                                        fileApp.convertToFile().extension
                                    }"
                                count++
                            }
                            Timber.d("dest $destinationPath --path ${SingletonPath.getInstance().path} -- else ${fileApp.convertToFile().parentFile?.absolutePath}")

                            fileApp.convertToFile().moveFileRecycleBin(destinationPath)
                            fileApp.convertToFile().delete()
//                        if (fileApp.favorite) {
//                            mainRepository.deleteFileFavorite(fileApp.id)
//                        }
                        }

                        fileApp.convertToFileHide(destinationPath)
                            ?.let { mainRepository.insertFileHide(it) }
                        SingletonListFileApp.getInstance().listFileApp.add(fileApp)
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess?.invoke()
                        }

                        //todo check
//                        _mediaStoreStateFlow.value = MediaStoreState.DELETE
                    }
                }
            }
        } catch (ex: Exception) {
            onFail?.invoke()
        }
    }

    fun prepareToProcess(type: FileStatus, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _fileStateFlow.value = FileState.PREPARE(type, path)
        }
    }

    fun compressFile(
        zipFileName: String,
        onLoading: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFail: ((message: String?) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedFileLiveData.value?.let { listFile ->
                var folderPath = "${File(listFile[0].path).parentFile?.absolutePath}/$zipFileName"
                try {
                    var isFile = false
                    var isDirectory = false
                    listFile.forEach { fileApp ->
                        if (fileApp.isDirectory()) {
                            isDirectory = true
                        } else {
                            isFile = true
                        }
                    }
                    when {
                        isFile && isDirectory -> {
                            viewModelScope.launch(Dispatchers.Main) {
                                onFail?.invoke(context.getString(R.string.notify_zip_file_and_folder))
                            }
                        }

                        listFile.size > 1 && isDirectory -> {
                            viewModelScope.launch(Dispatchers.Main) {
                                onFail?.invoke(context.getString(R.string.notify_zip_folders))
                            }
                        }

                        else -> {
                            viewModelScope.launch(Dispatchers.Main) {
                                onLoading?.invoke()
                            }
                            var count = 1
                            while (!File("$folderPath.zip").createNewFile()) {
                                folderPath = "$folderPath($count)"
                                count++
                            }
                            folderPath = "$folderPath.zip"
                            File(folderPath).delete()
                            val zipFile = ZipFile(folderPath)
                            zipFile.isRunInThread = true
                            if (isDirectory) {
                                zipFile.addFolder(listFile[0].convertToFile())
                            } else {
                                val list = mutableListOf<File>()
                                listFile.forEach { fileApp ->
                                    list.add(fileApp.convertToFile())
                                }
                                zipFile.addFiles(list)
                            }
                            viewModelScope.launch(Dispatchers.Main) {
                                onSuccess?.invoke()
                            }
                        }
                    }
                } catch (e: Exception) {
                    File(folderPath).delete()
                    Timber.tag(myTag).e("compressed faile: ${e.message}")
                    viewModelScope.launch(Dispatchers.Main) {
                        onFail?.invoke(context.getString(R.string.common_error))
                    }
                }
            }
        }
    }

    fun extractedFile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _fileStateFlow.value = FileState.LOADING("")
                selectedFileLiveData.value?.let { listFile ->
                    ZipFile(listFile[0].convertToFile()).extractAll(SingletonPath.getInstance().path)
                    _fileStateFlow.value = FileState.SUCCESS
                }
            } catch (ex: Exception) {
                _fileStateFlow.value = FileState.ERROR(ex.message.toString())
            }
        }
    }

    fun openZipFile(file: FileApp) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _fileStateFlow.value = FileState.LOADING("")
                ZipFile(file.convertToFile()).extractAll(SingletonPath.getInstance().path)
                _fileStateFlow.value = FileState.SUCCESS
            } catch (ex: Exception) {
                _fileStateFlow.value = FileState.ERROR(ex.message.toString())
            }
        }
    }

    fun createFolder(isAgreeCreate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _createFolderStateFlow.value = isAgreeCreate
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_fileStateFlow.value == FileState.START) {
                selectedFileLiveData.postValue(arrayListOf())
                selectedFolderLiveData.postValue(arrayListOf())
                selectedAppLiveData.postValue(arrayListOf())
                selectedAccountLiveData.postValue(arrayListOf())
                selectedFileDeleteLiveData.postValue(arrayListOf())
                sortedByLiveData.postValue("")
                multiLiveData.postValue(MultiSelect.Nothing)
            }
        }
    }

    fun clearFileState() {
        viewModelScope.launch(Dispatchers.IO) {
            _fileStateFlow.value = FileState.START
        }
    }

    fun clearMediaStoreState() {
        viewModelScope.launch(Dispatchers.IO) {
            _mediaStoreStateFlow.value = MediaStoreState.NOTHING
        }
    }

    fun getJunk() {
        var listJunk = arrayListOf<JunkType>()
        val rootPath = Environment.getExternalStorageDirectory().path
        val root = File(rootPath)
        junkFileLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                listJunk.addAll(getCachedJunk())
                listJunk.add(getAdware())
                listJunk.add(getApkJunk(root))
                junkFileLiveData.postSuccess(listJunk)
            } catch (e: Exception) {
                junkFileLiveData.postError(e.message)
            }
        }
    }

    private fun getCachedJunk(): ArrayList<JunkType> {
        val rootPath = Constants.INTERNAL_STORAGE_PATH
        val root = File(rootPath)
        val listAppCache = ArrayList<FileApp>()
        val listResidualJunk = ArrayList<FileApp>()
        root.listFiles()?.let {
            for (file in it) {
                val fileApp = FileApp()
                if (file.isDirectory) {
                    val listCacheFile = ArrayList<FileApp>()
                    var cacheSize = 0L
                    val pkgName = file.path.split("/").last()
                    val listCacheFolderInFolder = file.listFiles()?.filter { it.isCacheFolder() }
                    listCacheFolderInFolder?.let {
                        for (cacheFile in it) {
                            if (cacheFile.isDirectory) {
                                listCacheFile.addAll(FileUtils.getAllFilesInFolder(cacheFile))
                            } else {
                                cacheFile.convertToFileApp()?.let {
                                    listCacheFile.add(it)
                                }
                            }
                        }
                    }

                    listCacheFile.forEach {
                        cacheSize += it.size
                    }

                    if (cacheSize > 0 && listCacheFile.isNotEmpty()) {
                        fileApp.path = pkgName
                        fileApp.size = cacheSize
                        fileApp.type = Constants.PACKAGE_TYPE
                        fileApp.isSelected = true
                        fileApp.name = pkgName.getAppNameFromPkgName(context)
                        if (fileApp.name.isEmpty()) {
                            fileApp.name = fileApp.path
                            listResidualJunk.add(fileApp)
                        } else {
                            listAppCache.add(fileApp)
                        }
                    }
                }
            }
        }
        val itemAppCache = JunkType(
            context.getString(R.string.cache_junk),
            R.drawable.ic_cache_junk,
            0,
            true,
            true,
            listAppCache
        )

        val itemResidualJunk = JunkType(
            context.getString(R.string.residual_junk),
            R.drawable.ic_folder_junk,
            0,
            true,
            true,
            listResidualJunk
        )

        return arrayListOf(itemAppCache, itemResidualJunk)
    }

    private fun getApkJunk(root: File): JunkType {
        val apkFiles = ArrayList<FileApp>()
        val files = root.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    apkFiles.addAll(searchForApks(file))
                } else {
                    val fileName = file.name
                    if (fileName.lowercase().endsWith(Constants.APK_SUFFIX.lowercase())) {
                        file.convertToFileApp()?.let {
                            it.isSelected = true
                            apkFiles.add(it)
                        }
                    }
                }
            }
        }
        return JunkType(
            context.getString(R.string.apk_junk), R.drawable.ic_apk_junk, 0, true, true, apkFiles
        )
    }

    private fun getAdware(): JunkType {
        var size: Long = 0
        val files = File("/data/app/").listFiles()
        val listFile = arrayListOf<FileApp>()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory && file.name.lowercase()
                        .contains("ad") || file.name.lowercase().contains("adware")
                ) {
                    val subFiles = file.listFiles()
                    if (subFiles != null) {
                        for (subFile in subFiles) {
                            if (subFile.name.endsWith(".apk")) {
                                size += subFile.length()
                                subFile.convertToFileApp()?.let {
                                    it.isSelected = true
                                    listFile.add(it)
                                }
                            }
                        }
                    }
                }
            }
        }

        return JunkType(
            context.getString(R.string.ad_junk), R.drawable.ic_ad_junk, size, true, false, listFile
        )
    }

    private fun searchForApks(dir: File): ArrayList<FileApp> {
        val apkFiles = ArrayList<FileApp>()
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    apkFiles.addAll(searchForApks(file))
                } else {
                    val fileName = file.name
                    if (fileName.lowercase().endsWith(Constants.APK_SUFFIX.lowercase())) {
                        file.convertToFileApp()?.let {
                            it.isSelected = true
                            apkFiles.add(it)
                        }
                    }
                }
            }
        }
        return apkFiles
    }

    fun deleteListFiles(listFile: ArrayList<FileApp>) {
        deleteJunkFileLiveData.postLoading()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                listFile.forEach {
                    if (it.isPackageFolder()) {
                        FileUtils.deleteCacheInFolder(it)
                    } else {
                        val file = File(it.path)
                        if (file.exists() && file.isFile()) {
                            file.delete()
                        }
                    }
                }
                deleteJunkFileLiveData.postSuccess(true)
                //reload
                getJunk()
            } catch (e: Exception) {
                getJunk()
                deleteJunkFileLiveData.postError(e.message)
            }
        }
    }

    fun clickEvent(event: RegexModel) {
        viewModelScope.launch(Dispatchers.IO) {
            eventClick.postValue(event)
        }
    }

    companion object {
        private val RECENTS_LIMIT = 5
        private val RECENTS_BY_DATE_LIMIT = 100
    }
}