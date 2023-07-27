package com.phonecleaner.storagecleaner.cache.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseActivity
import com.phonecleaner.storagecleaner.cache.base.BasePagerAdapter
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.FileStatus
import com.phonecleaner.storagecleaner.cache.data.model.response.Screen
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.ActivityMainBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.handleOpenDocumentFile
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isDirectory
import com.phonecleaner.storagecleaner.cache.extension.isDocx
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isPdf
import com.phonecleaner.storagecleaner.cache.extension.isPptx
import com.phonecleaner.storagecleaner.cache.extension.isTxt
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isXlsx
import com.phonecleaner.storagecleaner.cache.extension.managerPostNotificationPermissionGranted
import com.phonecleaner.storagecleaner.cache.extension.managerStoragePermissionGranted
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.extension.requestPermissionManagerStorage
import com.phonecleaner.storagecleaner.cache.extension.shareApplication
import com.phonecleaner.storagecleaner.cache.extension.shareMultiFile
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.services.TrackingAppInstallService
import com.phonecleaner.storagecleaner.cache.ui.dialog.AudioDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.LogoutDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.PropertiesDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.VideoDialog
import com.phonecleaner.storagecleaner.cache.ui.fragment.CoolerContainerFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.DetailsFolderFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.ImagePreviewFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.ListFileFromFolderFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.MenuFileFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.MenuSettingFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.PreviewPdfFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.RecentFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.TextFragment
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.customView.BottomMenuView
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonDropboxMail
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonRecycleBinPath
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonScreen
import com.phonecleaner.storagecleaner.cache.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val selectedAccountList = mutableListOf<Account>()
    private val selectedFileDeleteList = mutableListOf<FileDelete>()
    private val selectedFileHideList = mutableListOf<FileHide>()
    private var zipFile: FileApp? = null
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    var dialog: Dialog? = null
    var detailsFolderFragment: DetailsFolderFragment? = null
    var onUninstallSS: (() -> Unit)? = null
    private val fragments: ArrayList<Fragment> = arrayListOf(
        MenuFileFragment(), CoolerContainerFragment(), RecentFragment(), MenuSettingFragment()
    )
    private val adapter = BasePagerAdapter(this@MainActivity, fragments)
    private val selectedAppList: MutableList<AppInstalled> = mutableListOf()
    val selectedFileList: MutableList<FileApp> = mutableListOf()
    val selectedFolderList: MutableList<Folder> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissionToPostNotification()
        checkPermissionToGetStorageInfo()
        setUpViewPager()
        setupDialog()
        binding.apply {
            layoutMenu.onClickEvent = object : BottomMenuView.OnClickEvent {
                override fun onSelectedType(type: SetMenuFunction) {
                    handleFunctionsBottomMenu(type)
                }
            }
        }
        if (managerPostNotificationPermissionGranted()) startServiceTrackingAppInstall()
    }

    fun deleteFile(isDeletePermanently: Boolean, fileApp: FileApp) {
        viewModel.deleteFile(isDeletePermanently = isDeletePermanently,
            fileApp = fileApp,
            onLoading = {
                dialogLoading()
            },
            onSuccess = {
                dialogDismiss()
            },
            onFail = {
                dialogDismiss()
                toast(getString(R.string.delete_fail))
            })
    }

    fun favoriteFile(fileApp: FileApp) {
        viewModel.handleFileFavorite(fileApp = fileApp, onLoading = {
            dialogLoading()
        }, onSuccess = {
            viewModel.getFavoriteFile(false)
            toast(getString(R.string.notify_add_favorite))
            dialogDismiss()
        }, onDeleteSuccess = {
            viewModel.getFavoriteFile(false)
            toast(getString(R.string.notify_remove_favorite))
            dialogDismiss()
        })
    }

    private fun checkActionClickNotification() {
//        when (intent.action) {
//            TrackingAppInstallService.ACTION_CLOSE_NOTI -> {
//                NotificationManagerCompat.from(this)
//                    .cancel(null, TrackingAppInstallService.NOTIFICATION_CLEAN_ID)
//            }
//
//            TrackingAppInstallService.ACTION_BOOST_NOW -> {
//                NotificationManagerCompat.from(this)
//                    .cancel(null, TrackingAppInstallService.NOTIFICATION_CLEAN_ID)
//            }
//
//            Constants.ACTION_FILE_MANAGER -> {
//            }
//
//            Constants.ACTION_CLEAN_JUNK -> {
//
//            }
//
//            Constants.BLOCK_NOTI -> {
//                regexScreenCleaner(RegexModel(type = Constants.REGEX_TYPE_BLOCK))
//            }
//
//        }
    }

    /**
     * Permission
     */
    private fun requestPermissionNotification() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun getLogoutState(isLogout: Boolean) {
        if (isLogout) {
            viewModel.logout()
        }
    }

    private fun setName(isSetName: Boolean, newName: String) {
        if (isSetName) {
            viewModel.setName(newName)
        }
    }

    private fun checkPermissionToGetStorageInfo() {
        if (managerStoragePermissionGranted()) {
            getStorageInfo()
        } else {
            requestPermissionManagerStorage(
                requestPermissionManageStorage, requestPermissionReadStorage
            )
        }
    }

    private fun checkPermissionToPostNotification() {
        if (!managerPostNotificationPermissionGranted()) {
            requestPermissionNotification()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionToPostNotification()
            }
        }
    }

    private var requestPermissionManageStorage: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Build.VERSION.SDK_INT >= 30) {
                if (Environment.isExternalStorageManager()) {
                    getStorageInfo()
                }
            }
        }

    private var requestPermissionReadStorage: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                getStorageInfo()
            }
        }

    /**
     * Service
     */
    private fun startServiceTrackingAppInstall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(
                    Intent(this@MainActivity, TrackingAppInstallService::class.java)
                )
            } else {
                startService(Intent(this@MainActivity, TrackingAppInstallService::class.java))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Dialog loading
     */
    private fun setupDialog() {
        dialog = Dialog(this, R.style.AlertDialog)
        dialog?.setContentView(
            LayoutInflater.from(this).inflate(R.layout.dialog_file_progress, null, false)
        )
        dialog?.setCancelable(false)
    }

    fun dialogLoading() {
        ValueAnimator.ofInt(0, 99).apply {
            duration = 1000
            addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Int
                dialog?.findViewById<ProgressBar>(R.id.progressBar)?.progress = animatorValue
                dialog?.findViewById<TextView>(R.id.tvProgressPercent)?.text = "$animatorValue%"
            }
            start()
        }
        dialog?.show()
    }

    fun dialogDismiss() {
        if (dialog != null && dialog!!.isShowing) dialog?.dismiss()
    }

    /**
     * Bottom menu
     */

    var cb: ((SetMenuFunction) -> Unit)? = null
    private fun handleFunctionsBottomMenu(type: SetMenuFunction) {
        cb?.invoke(type)
        when (type) {
            SetMenuFunction.COPY -> {
                handleCopyFunction()
            }

            SetMenuFunction.MOVE -> {
                handleMoveFunction()
            }

            SetMenuFunction.PASTE -> {
                when (SingletonMenu.getInstance().function) {
                    Constants.COPY -> {
                        if (selectedFileList.isNotEmpty()) {
                            viewModel.copyFile(onLoading = {
                                dialogLoading()
                            }, onSuccess = { list ->
                                viewModel.clearFileState()
                                viewModel.clearData()
                                detailsFolderFragment?.onPasteData(list)
                            }, onFail = { mgs ->
                                dialog?.dismiss()
                                mgs?.let { value ->
                                    toast(value)
                                }
                            })
                        } else if (selectedFolderList.isNotEmpty()) {
                            viewModel.copyFolder(onLoading = {
                                dialogLoading()
                            }, onSuccess = { list ->
                                viewModel.clearFileState()
                                viewModel.clearData()
                                detailsFolderFragment?.onPasteFolderData(list)
                            }, onFail = { mgs ->
                                dialog?.dismiss()
                                mgs?.let { value ->
                                    toast(value)
                                }
                            })
                        }
                    }

                    Constants.MOVE -> {
                        if (selectedFileList.isNotEmpty()) {
                            viewModel.moveFile(onLoading = {
                                dialogLoading()
                            }, onSuccess = { list ->
                                viewModel.clearFileState()
                                viewModel.clearData()
                                detailsFolderFragment?.onPasteData(list)
                            }, onFail = { mgs ->
                                dialog?.dismiss()
                                mgs?.let { value ->
                                    toast(value)
                                }
                            })
                        } else if (selectedFolderList.isNotEmpty()) {
                            viewModel.moveFolder(onLoading = {
                                dialogLoading()
                            }, onSuccess = { list ->
                                viewModel.clearFileState()
                                viewModel.clearData()
                                detailsFolderFragment?.onPasteFolderData(list)
                            }, onFail = { mgs ->
                                dialog?.dismiss()
                                mgs?.let { value ->
                                    toast(value)
                                }
                            })
                        }
                    }

                    else -> {}
                }
            }

            SetMenuFunction.EXTRACT -> {
                handleExtractFunction()
            }

            SetMenuFunction.CREATE -> {
                handleCreateFunction()
            }

            SetMenuFunction.CANCEL -> {
                handleCancelFunction()
            }

            SetMenuFunction.OPEN -> {
                handleOpenFunction()
            }

            SetMenuFunction.SHARE -> {
                handleShareFunction()
            }

            SetMenuFunction.ACCOUNT_RENAME -> {
                val dialog = RenameDialog(setName = ::setName)
                dialog.show(supportFragmentManager, Constants.DIALOG_RENAME)
            }

            SetMenuFunction.REMOVE -> {
                val dialog = LogoutDialog(getLogoutState = ::getLogoutState)
                dialog.show(supportFragmentManager, Constants.DIALOG_LOGOUT)
            }

            SetMenuFunction.DELETE_RECYCLE_BIN -> {
//                val dialog = RecycleBin(getStateDelete = ::getStateDelete)
//                dialog.show(supportFragmentManager, Constants.DIALOG_RECYCLE_BIN)
            }

            SetMenuFunction.RESTOCK -> {
                viewModel.restockFile()
            }

            SetMenuFunction.PROPERTIES -> {
                handlePropertiesFunction()
            }

            SetMenuFunction.APK_SHARE -> {
                this.shareApplication(selectedAppList[0].packageName, selectedAppList[0].appName)
            }

            SetMenuFunction.MOVE_TO_INTERNAL -> {
                Log.d("Test1234", "handleFunctionsBottomMenu: ${SingletonMenu.getInstance().type}")
                handleMoveToInternalFunction()
            }

            SetMenuFunction.MOVE_TO_DROPBOX -> {
                handleMoveToDropboxFunction()
            }

            else -> {}
        }
    }

    private fun handleMoveToInternalFunction() {
        try {
            detailsFolderFragment =
                DetailsFolderFragment.onSetupView(path = Environment.getExternalStorageDirectory().path)
            this.addFragment(detailsFolderFragment!!)

//            binding.layoutMenu.show()
            SingletonScreen.getInstance().type = Screen.Internal
            viewModel.prepareToProcess(FileStatus.COPY, "")
        } catch (ex: Exception) {
            Timber.tag(myTag).e("move screen failed: ${ex.message}")
        }
    }

    private fun handleMoveToDropboxFunction() {
        var uploadFailed = false
        binding.layoutMenu.show()
        if (selectedFileList.isNotEmpty()) {
            for (file in selectedFileList) {
                if (file.isDirectory()) {
                    uploadFailed = true
                    break
                }
            }
        } else if (selectedFolderList.isNotEmpty()) {
            uploadFailed = true
        }
        if (uploadFailed) {
            this.toast(getString(R.string.notify_upload_drop_box))
        } else {
            intent = Intent(this, DropBoxActivity::class.java)
            intent.putExtra(
                Constants.LIST_FILE_UPDATE_DROP_BOX, Gson().toJson(selectedFileList)
            )
            intent.putExtra(Constants.DROPBOX_EMAIL, SingletonDropboxMail.getInstance().mail)
            SingletonMenu.getInstance().type = Constants.MENU_HANDLER
            SingletonScreen.getInstance().type = Screen.Dropbox
            startActivity(intent)
        }
    }

    private fun handleCopyFunction() {
        SingletonMenu.getInstance().type = Constants.MENU_MOVE_TO
        SingletonMenu.getInstance().function = Constants.COPY
        binding.layoutMenu.initData(getString(R.string.copy_to))
        binding.layoutMenu.show()
    }

    private fun handleMoveFunction() {
        SingletonMenu.getInstance().type = Constants.MENU_MOVE_TO
        SingletonMenu.getInstance().function = Constants.MOVE
        binding.layoutMenu.initData(this@MainActivity.getString(R.string.move_to))
        binding.layoutMenu.show()
    }

    private fun handlePasteFunction() {
        when (SingletonMenu.getInstance().function) {
            Constants.COPY -> {
                if (selectedFileList.isNotEmpty()) {
                    viewModel.copyFile(onLoading = {
                        dialogLoading()
                    }, onSuccess = { list ->
                        viewModel.clearFileState()
                        viewModel.clearData()
                        detailsFolderFragment?.onPasteData(list)
                    }, onFail = { mgs ->
                        dialog?.dismiss()
                        mgs?.let { value ->
                            toast(value)
                        }
                    })
                } else if (selectedFolderList.isNotEmpty()) {
                    viewModel.copyFolder(onLoading = {
                        dialogLoading()
                    }, onSuccess = { list ->
                        viewModel.clearFileState()
                        viewModel.clearData()
                        detailsFolderFragment?.onPasteFolderData(list)
                    }, onFail = { mgs ->
                        dialog?.dismiss()
                        mgs?.let { value ->
                            toast(value)
                        }
                    })
                }
            }

            Constants.MOVE -> {
                if (selectedFileList.isNotEmpty()) {
                    viewModel.moveFile(onLoading = {
                        dialogLoading()
                    }, onSuccess = { list ->
                        viewModel.clearFileState()
                        viewModel.clearData()
                        detailsFolderFragment?.onPasteData(list)
                    }, onFail = { mgs ->
                        dialog?.dismiss()
                        mgs?.let { value ->
                            toast(value)
                        }
                    })
                } else if (selectedFolderList.isNotEmpty()) {
                    viewModel.moveFolder(onLoading = {
                        dialogLoading()
                    }, onSuccess = { list ->
                        viewModel.clearFileState()
                        viewModel.clearData()
                        detailsFolderFragment?.onPasteFolderData(list)
                    }, onFail = { mgs ->
                        dialog?.dismiss()
                        mgs?.let { value ->
                            toast(value)
                        }
                    })
                }
            }

            else -> {}
        }
    }

    private fun handleExtractFunction() {
        if (selectedFileList.isNotEmpty()) {
            viewModel.extractedFile()
        }
    }

    private fun handleCancelFunction() {
        viewModel.clearData()
        viewModel.clearFileState()
        SingletonMenu.getInstance().type = -1
        binding.layoutMenu.show()
    }

    private fun handleOpenFunction() {
        when {
            selectedFileList.isNotEmpty() -> {
                navigateToOpenFileScreen(selectedFileList[0])
            }

            selectedFolderList.isNotEmpty() -> {
                navigateToOpenFolderScreen(selectedFolderList[0])
            }
        }
    }

    private fun handleShareFunction() {
        val pathList = mutableListOf<String>()
        var shareFailed = false
        if (selectedFileList.isNotEmpty()) {
            for (fileApp in selectedFileList) {
                if (File(fileApp.path).isDirectory) {
                    shareFailed = true
                    break
                }
                pathList.add(fileApp.path)
            }
        }
        if (shareFailed) {
            this.toast(getString(R.string.notify_share_folder))
        } else {
            this.shareMultiFile(pathList, type = "*")
        }
    }

    private fun handlePropertiesFunction() {
        val propertiesDialog = PropertiesDialog()
        when {
            selectedFileList.isNotEmpty() -> {
                propertiesDialog.setFileApp(selectedFileList[0])
            }

            selectedFolderList.isNotEmpty() -> {
                propertiesDialog.setFolder(selectedFolderList[0])
            }

            selectedAppList.isNotEmpty() -> {
                propertiesDialog.setAppInstalled(selectedAppList[0])
            }
        }
        propertiesDialog.show(supportFragmentManager, Constants.DIALOG_PROPERTIES)
    }

    fun navigateToOpenAccountScreen(account: Account) {
        intent = Intent(this, DropBoxActivity::class.java)
        intent.putExtra(Constants.DROPBOX_EMAIL, account.email)
        startActivity(intent)
    }

    private fun handleCreateFunction() {
        viewModel.createFolder(true)
    }

    private fun setUpViewPager() {
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fragment_my_file -> {
                    this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
                    binding.viewPager.currentItem = 0
                    true
                }

                R.id.fragment_cooler -> {
                    this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    this.window.statusBarColor = ContextCompat.getColor(this, R.color.white)
                    binding.viewPager.currentItem = 1
                    true
                }

                R.id.fragment_recent -> {
                    this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    this.window.statusBarColor = ContextCompat.getColor(this, R.color.blueCooler)
                    binding.viewPager.currentItem = 2
                    true
                }

                R.id.fragment_setting -> {
                    this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    this.window.statusBarColor = ContextCompat.getColor(this, R.color.blueCooler)
                    binding.viewPager.currentItem = 3
                    true
                }

                else -> {
                    true
                }
            }
        }
    }

    fun hideNavigationView() {
        binding.bottomNav.visibility = View.GONE
    }

    fun showNavigationView() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    fun navigateToOpenFileScreen(file: FileApp) {
        when {
            file.isVideo() -> {
                val videoDialog = VideoDialog.onSetupView(file)
                videoDialog.show(supportFragmentManager, Constants.DIALOG_VIDEO)
            }

            file.isAudio() -> {
                val audioDialog = AudioDialog.onSetupView(file)
                audioDialog.show(supportFragmentManager, Constants.DIALOG_AUDIO)
            }

            file.isPdf() -> {
                val pagerView = PreviewPdfFragment.onSetupView(fileApp = file)
                pagerView.show(supportFragmentManager, Constants.DIALOG_PDF_PREVIEW)
            }

            file.isPptx() || file.isXlsx() -> {
                this.handleOpenDocumentFile(file.path)
            }

            file.isApk() -> {
                openIntentInstallApk(fileAppApk = file)
            }

            file.isTxt() -> {
                val pagerView = TextFragment.onSetupView(fileApp = file)
                pagerView.show(supportFragmentManager, Constants.DIALOG_DOCUMENT)
            }

            file.isDirectory() -> {
                val pagerView = DetailsFolderFragment.onSetupView(path = file.path)
                this.addFragment(pagerView)
            }

            file.isImage() -> {
                val imagePreview = ImagePreviewFragment.onSetupView(file)
                imagePreview.show(supportFragmentManager, Constants.DIALOG_IMAGE_PREVIEW)
            }

            file.isDocx() -> {
                /**
                 * Open file word need fix
                 */
                val fileUri = Uri.fromFile(file.convertToFile())
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(fileUri, "resource/folder");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val chooser =
                    Intent.createChooser(intent, this@MainActivity.getString(R.string.app_name))
                try {
                    this@MainActivity.startActivity(chooser)
                } catch (ex: ActivityNotFoundException) {
                    Timber.tag(myTag).e("Exception ${ex.message}")
                }
            }
        }
    }

    private fun openIntentInstallApk(fileAppApk: FileApp) {
        try {
            val fileApk = File(fileAppApk.path)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkUri = FileProvider.getUriForFile(
                    this, applicationContext.packageName + ".provider", fileApk
                )
                intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                intent.data = apkUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            } else {
                val apkUri = Uri.fromFile(fileApk)
                intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (ex: Exception) {
            Timber.tag(myTag).e("Install apk failed: ${ex.message}")
        }
    }

    fun navigateToOpenAppScreen(file: AppInstalled) {
        // check mime type cua file
        // neu la anh thi navigate to detailsImageFragment
        // neu la file kahc thi intent.action view
    }

    fun uninstallApp() {
        val uri: Uri = Uri.fromParts("package", selectedAppList[0].packageName, null)
        val uninstallIntent = Intent(Intent.ACTION_DELETE, uri)
        resultLauncher.launch(uninstallIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 0) {
                onUninstallSS?.invoke()
                viewModel.clearData()
            }
        }

    fun navigateToOpenFolderScreen(folder: Folder) {
        try {
            val pagerView: ListFileFromFolderFragment =
                ListFileFromFolderFragment.onSetupView(folder = folder)
            this.addFragment(pagerView)
        } catch (ex: IllegalArgumentException) {
            Timber.tag(myTag).e("move screen failed: ${ex.message}")
        }
    }

    private fun getStorageInfo() {
        initObserve()
        initFileState()
        createRecycleBin()
//        viewModel.getStorageInfo()
//        viewModel.getRecentFile()
//        viewModel.getJunk()
    }

    private fun initFileState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fileStateFlow.collect { fileState ->
                    when (fileState) {
                        is FileState.START -> {
                            Timber.tag(myTag).d("FileState.START")
                        }

                        is FileState.PREPARE -> {
                            Timber.tag(myTag).d("FileState.PREPARE")
                            if (fileState.type == FileStatus.COPY) {
                                SingletonMenu.getInstance().type = Constants.MENU_HANDLER
                            }
                            if (fileState.type == FileStatus.EXTRACT) {
                                SingletonMenu.getInstance().type = Constants.MENU_EXTRACT
                            }
                            binding.layoutMenu.show()
                        }

                        is FileState.LOADING -> {
                            Timber.tag(myTag).d("FileState.LOADING")
                        }

                        is FileState.SUCCESS -> {
                            Timber.tag(myTag).d("FileState.SUCCESS")
                            clearList()
                            SingletonMenu.getInstance().type = -1
                            binding.layoutMenu.show()
                        }

                        is FileState.ERROR -> {
                            Timber.tag(myTag).d("FileState.ERROR")
                            clearList()
                            SingletonMenu.getInstance().type = -1
                            binding.layoutMenu.show()
                            this@MainActivity.toast(fileState.message)
                        }
                    }
                }
            }
        }
    }

    private fun clearList() {
        selectedFileList.clear()
        selectedFolderList.clear()
        selectedAppList.clear()
        selectedAccountList.clear()
        selectedFileDeleteList.clear()
        selectedFileHideList.clear()
        zipFile = null
    }

    private fun createRecycleBin() {
        val recycleBinDirectory: File = getDir(getString(R.string.recycle), Context.MODE_PRIVATE)
        try {
            if (!recycleBinDirectory.exists()) {
                recycleBinDirectory.mkdir()
            }
        } catch (ex: SecurityException) {
            Timber.tag(myTag).e("create recycle bin failed: ${ex.message}")
        }
        SingletonRecycleBinPath.getInstance().path = recycleBinDirectory.path
    }

    private fun initObserve() {
        with(viewModel) {
            //Observe list select in MainViewModel if list is Empty hide menu or opposite
            observe(selectedFileLiveData) {
                it?.let {
                    if (it.isNotEmpty()) {
                        binding.layoutMenu.initUi(it.size)
                        SingletonMenu.getInstance().type = Constants.MENU_NORMAL
                        selectedFileList.clear()
                        selectedFileList.addAll(it)
                    } else {
                        SingletonMenu.getInstance().type = -1
                    }
                    binding.layoutMenu.show()
                }
            }
            observe(selectedFolderLiveData) {
                it?.let {
                    if (it.isNotEmpty()) {
                        binding.layoutMenu.initUi(it.size)
                        SingletonMenu.getInstance().type = Constants.MENU_ALBUM
                        selectedFolderList.clear()
                        selectedFolderList.addAll(it)
                    } else {
                        SingletonMenu.getInstance().type = -1
                    }
                    binding.layoutMenu.show()
                }
            }
            observe(selectedAccountLiveData) {
                it?.let {
                    if (it.isNotEmpty()) {
                        SingletonMenu.getInstance().type = Constants.MENU_ACCOUNT
                    } else {
                        SingletonMenu.getInstance().type = -1
                    }
                    binding.layoutMenu.initUi(it.size)
                    binding.layoutMenu.show()
                }
            }
            observe(selectedAppLiveData) {
                it?.let {
                    if (it.isNotEmpty()) {
                        binding.layoutMenu.initUi(it.size)
                        SingletonMenu.getInstance().type = Constants.MENU_APP
                        selectedAppList.clear()
                        selectedAppList.addAll(it)
                    } else {
                        SingletonMenu.getInstance().type = -1
                    }
                    binding.layoutMenu.show()
                }
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE: Int = 1001
    }
//    fun regexScreenCleaner(type: RegexModel) {
//        if (binding.viewPager.currentItem != 1) {
//            AdsFullManager.showInterstitial(this@MainActivity) {
//                binding.viewPager.currentItem = 1
//                binding.bottomView.selectedItemId = R.id.id_cleaner
//            }
//        } else {
//            binding.viewPager.currentItem = 1
//            binding.bottomView.selectedItemId = R.id.id_cleaner
//        }
//        viewModel.clearData()
//        viewModel.clickEvent(type)
//    }


    val celsiusPhoneLiveData = MutableLiveData<String>()


    // Get temperature celsius phone
    val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                celsiusPhoneLiveData.value = "${event.values[0]}"
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    fun getTemperatureCelsius() {
        var sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        var temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        sensorManager.registerListener(
            sensorListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}