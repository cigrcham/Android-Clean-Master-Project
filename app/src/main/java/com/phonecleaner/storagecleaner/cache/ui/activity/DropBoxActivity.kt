package com.phonecleaner.storagecleaner.cache.ui.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.files.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phonecleaner.storagecleaner.cache.base.dropBox.BaseDropbox
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DownloadFileTaskResult
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxUploadApiResponse
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.GetCurrentAccountResult
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.ListFolderApiResult
import com.phonecleaner.storagecleaner.cache.base.dropBox.internal.ui.FilesAdapter
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.ActivityDropBoxBinding
import com.phonecleaner.storagecleaner.cache.extension.getExtensionFile
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.dialog.PropertiesDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.customView.BottomMenuView
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonDropboxMail
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.DropboxViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

@AndroidEntryPoint
class DropBoxActivity : BaseDropbox(),View.OnClickListener{
    private val myTag = "DropboxScreen"

    private lateinit var binding: ActivityDropBoxBinding
    private val dropboxViewModel by viewModels<DropboxViewModel>()
    private val dataViewModel by viewModels<DataViewModel>()
    private val filesAdapter = FilesAdapter(
        loadThumbnail = ::loadThumbnail,
        itemOnClickListener = ::itemOnClickListener,
        itemOnSelectListener = ::itemOnSelectListener
    )
    private val listMetaData = mutableListOf<Metadata>()
    private val listFileUpload = mutableListOf<FileApp>()
    private val fileList = mutableListOf<Metadata>()
    private var fileState = NOTHING
    private var viewFrame = 0
    private var filePath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDropBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grayF6F6F6)
        dataViewModel.getAllAccounts(Constants.DROPBOX_EMAIL)

        intent.getStringExtra(Constants.DROPBOX_EMAIL).let { mail ->
            if (mail?.isEmpty() == true || mail == null) {
                if (isAuthenticated()) {
                    dropboxOAuthUtil.revokeDropboxAuthorization(dropboxApiWrapper)
                } else {
                    dropboxOAuthUtil.startDropboxAuthorization(this)
                }
            } else {
                if (!isAuthenticated()) {
                    dropboxOAuthUtil.startDropboxAuthorization(this)
                } else {
                    if (SingletonDropboxMail.getInstance().mail != mail) {
                        dropboxOAuthUtil.revokeDropboxAuthorization(dropboxApiWrapper)
                        finish()
                    }
                }
            }
        }

        binding.layoutBottomMenu.onClickEvent = object : BottomMenuView.OnClickEvent {
            override fun onSelectedType(type: SetMenuFunction) {
                handleFunctionsBottomMenu(type)
            }
        }
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (!isAuthenticated()) {
            binding.progressBar.isVisible = true
            this.lifecycleScope.launch {
                try {
                    delay(5000)
                    dropboxOAuthUtil.startDropboxAuthorization(this@DropBoxActivity)
                } catch (e: Exception) {
                    Timber.tag(myTag).e("authorization failed")
                    finish()
                }
            }
        }
    }

    override fun loadData() {
        binding.progressBar.isGone = true
        checkLogin()
        initData()
        initUi()
        observeListAccount()
    }

    private val listAccount = mutableListOf<Account>()
    private fun observeListAccount() {
        dataViewModel.getAllAccount.observe(this) { list ->
            listAccount.clear()
            listAccount.addAll(list)
        }
    }

    private fun checkLogin() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val accountResult = dropboxApiWrapper.getCurrentAccount()) {
                is GetCurrentAccountResult.Error -> {
                    finish()
                }
                is GetCurrentAccountResult.Success -> {
                    Timber.tag(myTag).d("GetCurrentAccountResult.Success")
                    intent.getStringExtra(Constants.DROPBOX_EMAIL).let { mail ->
                        if (mail == null || mail.isEmpty()) {
                            val account = Account()
                            account.name = accountResult.account.name.displayName
                            account.email = accountResult.account.email
                            account.type = Constants.DROPBOX_EMAIL
                            var isExist = false
                            listAccount.forEach { item ->
                                if (item.email == account.email) {
                                    isExist = true
                                    return@forEach
                                }
                            }
                            if (!isExist) {
                                dataViewModel.insertAccount(account)
                                listAccount.add(account)
                            }
                        }
                    }
                    SingletonDropboxMail.getInstance().mail = accountResult.account.email
                    dataViewModel.setDropboxMail(accountResult.account.email)
                }
            }
        }
    }

    private fun initData() {
        dropboxViewModel.loadData(dropboxApiWrapper, filePath)
        this.lifecycleScope.launch {
            this@DropBoxActivity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dropboxViewModel.resultApiState.collect { result ->
                    when (result) {
                        is FileState.SUCCESS -> {
                            dropboxViewModel.fileList.collect { list ->
                                if (list?.size == 0) {
                                    binding.imgFileNotFound.visibility = View.VISIBLE
                                } else {
                                    filesAdapter.setFiles(list)
                                    fileList.clear()
                                    list?.let { fileList.addAll(it) }
                                }
                                if (fileState < FINISH) {
                                    intent.getStringExtra(Constants.LIST_FILE_UPDATE_DROP_BOX)
                                        ?.let {
                                            fileState = COPY
                                            val typeToken =
                                                object : TypeToken<MutableList<FileApp>>() {}.type
                                            listFileUpload.clear()
                                            listFileUpload.addAll(
                                                Gson().fromJson<MutableList<FileApp>>(
                                                    it,
                                                    typeToken
                                                )
                                            )
                                            if (listFileUpload.isNotEmpty()) {
                                                binding.layoutBottomMenu.show()
                                            }
                                        }
                                }
                            }
                        }
                        is FileState.ERROR -> {
                            binding.imgFileNotFound.visibility = View.VISIBLE
                        }
                        else -> {}
                    }
                }

            }
        }
        binding.tvTitle.text = if (filePath.isBlank()) Constants.DROP_BOX else File(filePath).name
        if (fileState == COPY) {
            listMetaData.clear()
        }
    }

    private fun initUi() {
        binding.rcvDropbox.layoutManager = WrapContentLinearLayoutManager(this)
        binding.rcvDropbox.adapter = filesAdapter
    }

    private fun initListener() {
        binding.imgBack.setOnClickListener(this)
        binding.imgChangeView.setOnClickListener(this)
        binding.imgOpenMenu.setOnClickListener(this)
        binding.imgSelectAll.setOnClickListener(this)
        binding.tvSortBy.setOnClickListener(this)
    }

    private fun loadThumbnail(file: FileMetadata, imageView: ImageView) {
        lifecycleScope.launch {
            val byteBuffer = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                val downloader: DbxDownloader<FileMetadata> =
                    dropboxApiWrapper.dropboxClient.files()
                        .getThumbnailBuilder(file.pathLower)
                        .withFormat(ThumbnailFormat.JPEG)
                        .withSize(ThumbnailSize.W1024H768)
                        .start()
                downloader.download(byteBuffer)
            }
            Glide.with(imageView.context)
                .load(byteBuffer.toByteArray())
                .placeholder(android.R.drawable.ic_popup_sync)
                .error(android.R.drawable.sym_def_app_icon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    private fun itemOnClickListener(file: Metadata) {
        when (file) {
            is FileMetadata -> {}
            is FolderMetadata -> {
                filePath = file.pathLower
                initData()
            }
        }
    }

    private fun itemOnSelectListener(metadata: Metadata, isChecked: Boolean) {
        if (isChecked) {
            listMetaData.add(metadata)
        } else {
            listMetaData.remove(metadata)
        }
        if (listMetaData.isNotEmpty()) {
            SingletonMenu.getInstance().type = Constants.MENU_DROP_BOX
        } else {
            SingletonMenu.getInstance().type = -1
        }
        binding.layoutBottomMenu.initUi(listMetaData.size)
        binding.layoutBottomMenu.show()
    }

    private fun handleFunctionsBottomMenu(type: SetMenuFunction) {
        when (type) {
            SetMenuFunction.DOWNLOAD -> {
                handleDownloadFunction()
            }
            SetMenuFunction.PROPERTIES -> {
                val propertiesDialog = PropertiesDialog()
                propertiesDialog.setFileDropbox(listMetaData[0])
                propertiesDialog.show(supportFragmentManager, Constants.DIALOG_PROPERTIES)
            }
            SetMenuFunction.PASTE -> {
                handlePasteFunction()
            }
            SetMenuFunction.CREATE -> {
                this.toast(getString(R.string.notify_feature_google))
            }
            SetMenuFunction.CANCEL -> {
                listFileUpload.clear()
                fileState = FINISH
                SingletonMenu.getInstance().type = -1
                binding.layoutBottomMenu.show()
            }
            else -> {}
        }
    }

    private fun handleDownloadFunction() {
        var isCheckFolder = false
        for (file in listMetaData) {
            if (file is FolderMetadata) {
                isCheckFolder = true
                break
            }
        }
        if (isCheckFolder) {
            this.toast(getString(R.string.notify_failed_folder))
        } else {
            try {
                listMetaData.forEach { metadata ->
                    binding.progressBar.isVisible = true
                    downloadFile(metadata as FileMetadata)
                }
            } catch (ex: Exception) {
                Timber.tag(myTag).e("move screen failed: ${ex.message}")
            }
        }
    }

    private fun handlePasteFunction() {
        binding.progressBar.isVisible = true
        lifecycleScope.launch {
            var response: DropboxUploadApiResponse? = null
            for (i in listFileUpload.indices) {
                val uri = Uri.fromFile(File(listFileUpload[i].path))
                val inputStream = contentResolver.openInputStream(uri)
                val name = uri.lastPathSegment
                inputStream?.let { input ->
                    name?.let {
                        response = dropboxApiWrapper.uploadFile(it, input, filePath)
                        input.close()
                    }
                }
            }
            when (response) {
                is DropboxUploadApiResponse.Failure -> {}
                is DropboxUploadApiResponse.Success -> {
                    lifecycleScope.launch {
                        when (val apiResult = dropboxApiWrapper.listFolders(filePath)) {
                            is ListFolderApiResult.Error -> {}
                            is ListFolderApiResult.Success -> {
                                try {
                                    filesAdapter.setFiles(apiResult.result.entries)
                                    SingletonMenu.getInstance().type = -1
                                    binding.layoutBottomMenu.show()
                                    listFileUpload.clear()
                                    fileState = FINISH
                                    binding.progressBar.isGone = true
                                } catch (e: Exception) {
                                    Timber.tag(myTag).e("update failed: ${e.message}")
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun downloadFile(file: FileMetadata) {
        lifecycleScope.launch {
            val downloadFileTaskResult = dropboxApiWrapper.download(
                this@DropBoxActivity.applicationContext, file
            )
            when (downloadFileTaskResult) {
                is DownloadFileTaskResult.Error -> {
                    binding.progressBar.isGone = true
                    this@DropBoxActivity.toast("${getString(R.string.notify_download_failed)} ${file.name}")
                }
                is DownloadFileTaskResult.Success -> {
                    binding.progressBar.isGone = true
                    this@DropBoxActivity.toast("${getString(R.string.notify_download_success)} ${file.name}")
                }
            }
        }
    }

    private fun sortedByCallback(isAccept: Boolean, condition: String) {
        if (isAccept) {
            when (condition) {
                Constants.SORTED_BY_NAME_FROM_A_TO_Z -> {
                    fileList.sortBy { metadata ->
                        metadata.name
                    }
                }
                Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                    fileList.sortByDescending { metadata ->
                        metadata.name
                    }
                }
                Constants.SORTED_BY_DATE -> {
                    try {
                        fileList.sortBy { metadata ->
                            (metadata as FileMetadata).serverModified.time
                        }
                    } catch (e: Exception) {
                        this.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }
                Constants.SORTED_BY_SIZE -> {
                    try {
                        fileList.sortBy { metadata ->
                            (metadata as FileMetadata).size
                        }
                    } catch (e: Exception) {
                        this.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }
                Constants.SORTED_BY_TYPE -> {
                    try {
                        fileList.sortBy { metadata ->
                            (metadata as FileMetadata).name.getExtensionFile()
                        }
                    } catch (e: Exception) {
                        this.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }
                else -> {}
            }
            filesAdapter.setFiles(fileList)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.imgBack -> {
                onBackPressed()
            }
            binding.imgChangeView -> {
                if (viewFrame == 0) {
                    viewFrame = 1
                    binding.imgChangeView.setImageResource(R.drawable.ic_toolbar_change_view_grid)
                    binding.rcvDropbox.layoutManager = GridLayoutManager(this, SPAN_COUNT)

                } else {
                    viewFrame = 0
                    binding.imgChangeView.setImageResource(R.drawable.ic_toolbar_change_view_linear)
                    binding.rcvDropbox.layoutManager = WrapContentLinearLayoutManager(this)
                }
                filesAdapter.changeView(viewFrame)
            }
            binding.imgOpenMenu -> {
                binding.layoutMenu.isGone = !binding.layoutMenu.isGone
            }
            binding.imgSelectAll -> {
                filesAdapter.selectAll()
                listMetaData.clear()
                listMetaData.addAll(fileList)
                SingletonMenu.getInstance().type = Constants.MENU_DROP_BOX
                binding.layoutBottomMenu.initUi(listMetaData.size)
                binding.layoutBottomMenu.show()
            }
            binding.tvSortBy -> {
                val sortedDialog =
                    SortedDialog(
                        screenType = PagerType.DOWNLOAD,
                        sortedByCallback = ::sortedByCallback
                    )
                sortedDialog.isCancelable = false
                sortedDialog.show(supportFragmentManager, Constants.DIALOG_SORT_BY)
            }
            else -> {}
        }
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAuthenticated() && SingletonDropboxMail.getInstance().mail.isNotEmpty()) {
                    filePath = File(filePath).parentFile?.absolutePath ?: ""
                    if (filePath.isBlank()) {
                        finish()
                    } else if (filePath == "/") {
                        filePath = ""
                    }
                    initData()
                }
                else {
                    finish()
                }
            }
        })
        super.onBackPressed()
    }

    companion object {
        private const val SPAN_COUNT = 4
        private const val NOTHING = 0
        private const val COPY = 1
        private const val FINISH = 2
    }
}