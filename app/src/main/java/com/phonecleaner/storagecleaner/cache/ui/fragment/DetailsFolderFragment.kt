package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentDetailsFolderBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.dp2px
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.adapters.HeaderExplorerAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.ZipFileDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.CommonItemDecoration
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonPath
import com.phonecleaner.storagecleaner.cache.utils.visible
import com.phonecleaner.storagecleaner.cache.viewmodel.DetailsFolderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Stack

/**
 * Open Internal in HomeFragment
 */
@AndroidEntryPoint
class DetailsFolderFragment : BaseFragment(), View.OnClickListener {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentDetailsFolderBinding
    private val viewModel: DetailsFolderViewModel by viewModels()
    private val fileList: ArrayList<FileApp> = arrayListOf()
    private val explorerAdapter = FilesAdapter()
    private val headerExplorerAdapter = HeaderExplorerAdapter()
    private var folderPath: String? = null
    private val listPositionSelected: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsFolderBinding.inflate(inflater, container, false)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        return binding.root
    }

    override fun initData() {
        folderPath?.let { folderPath ->
            File(folderPath).convertToFileApp()?.let { fileApp ->
                viewModel.addFolderToStack(fileApp)
                binding.toolbar.tvTitle.text = fileApp.name
                SingletonPath.getInstance().path = folderPath
            }
            handleOpenDirectory()
        }
    }

    override fun initUi() {
        initHeaderExplorerRecycler()
        initExplorerRecycler()
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener(this)
        binding.toolbar.imgOpenMenu.setOnClickListener(this)
        binding.toolbar.tvSortBy.setOnClickListener(this)
        binding.toolbar.tvCreateFolder.setOnClickListener(this)
        binding.toolbar.ctlView.setOnClickListener(this)
        binding.toolbar.fmDisplay.setOnClickListener(this)
        binding.toolbar.tvList.setOnClickListener(this)
        binding.toolbar.tvGrid.setOnClickListener(this)
    }

    override fun doWork() {
        initFileState()
        observeCreateFolderState()
        initObserve()
    }

    private fun initFileState() {
        context?.let { ct ->
            val dialog = Dialog(ct, R.style.PropertiesDialog)
            dialog.setContentView(
                LayoutInflater.from(ct).inflate(R.layout.dialog_file_progress, null, false)
            )
            dialog.setCancelable(true)
            var currentProgress = 1
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mainViewModel.fileStateFlow.collect { fileState ->
                        when (fileState) {
                            is FileState.START -> {
                                if (dialog.isShowing) {
                                    dialog.dismiss()
                                }
                            }

                            is FileState.PREPARE -> {
                                Timber.tag(myTag).d("FileState.PREPARE")
                                if (fileState.path.isNotEmpty()) {
                                    File(fileState.path).convertToFileApp()?.let { fileApp ->
                                        viewModel.addFolderToStack(fileApp)
                                        binding.toolbar.tvTitle.text = fileApp.name
                                        SingletonPath.getInstance().path = fileState.path
                                    }
                                }
                            }

                            is FileState.LOADING -> {
                                Timber.tag(myTag).d("FileState.LOADING")
                                dialog.show()
                                while (currentProgress <= 99) {
                                    dialog.findViewById<ProgressBar>(R.id.progressBar).progress =
                                        currentProgress
                                    dialog.findViewById<TextView>(R.id.tvProgressPercent).text =
                                        "$currentProgress%"
                                    currentProgress++
                                }
                            }

                            is FileState.SUCCESS -> {
                                Timber.tag(myTag).d("FileState.SUCCESS")
                                viewModel.getAllFileFromFolder(SingletonPath.getInstance().path)
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                dialog.dismiss()
                            }

                            is FileState.ERROR -> {
                                Timber.tag(myTag).d("FileState.ERROR")
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeCreateFolderState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.createFolderStateFlow.collect { isAgree ->
                    if (isAgree) {
                        val dialog = ZipFileDialog(
                            getString(R.string.create_folder), setName = ::setNameFolder
                        )
                        dialog.show(childFragmentManager, Constants.DIALOG_CREATE_FOLDER)
                        mainViewModel.createFolder(false)
                    }
                }
            }
        }
    }

    private fun initObserve() {
        with(viewModel) {
            observe(allFileLiveData, ::handleGetAllFile)
            observe(stackFolder, ::handleStackChange)
        }
        with(mainViewModel) {
            observe(sortedByLiveData, ::sortedByForCondition)
            observe(multiLiveData, ::handleEventFromMain)
        }
    }

    private fun initHeaderExplorerRecycler() {
        context?.let {
            binding.headerRecycler.layoutManager =
                WrapContentLinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
            binding.headerRecycler.adapter = headerExplorerAdapter
            headerExplorerAdapter.onClickEvent = { fileApp, _ ->
                viewModel.popToFolder(fileApp)
            }
        }
    }

    val onItemClick: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            if (File(file.path).isDirectory) {
                viewModel.addFolderToStack(file)
                SingletonPath.getInstance().path = file.path
            } else {
                onClickEvent.onClickItem(file, position)
            }
        }

        override fun onSelectItem(file: FileApp, position: Int) {
            mainViewModel.onSelectFile(file, position)
            if (listPositionSelected.contains(position)) {
                listPositionSelected.remove(position)
            } else {
                listPositionSelected.add(position)
            }
            SingletonMenu.getInstance().type = Constants.MENU_NORMAL
            activity?.let {
                (it as MainActivity).cb = { data ->
                    when (data) {
                        // When click delete in menu
                        SetMenuFunction.DELETE -> {
                            val dialog =
                                DeleteDialog(getStateDelete = { isDeletePermanently: Boolean, isDelete: Boolean ->
                                    if (isDelete) {
                                        if (it.selectedFileList.isNotEmpty()) {
                                            mainViewModel.deleteFile(isDeletePermanently,
                                                onLoading = { it.dialogLoading() },
                                                onSuccess = {
                                                    mainViewModel.clearFileState()
                                                    mainViewModel.clearData()
                                                    it.dialog?.dismiss()
                                                    explorerAdapter.onDeleteFile(
                                                        listPositionSelected
                                                    )
                                                    listPositionSelected.clear()
                                                },
                                                onFail = {
                                                    it.dialog?.dismiss()
                                                    mainViewModel.clearFileState()
                                                    mainViewModel.clearData()
                                                })
                                        } else if (it.selectedFolderList.isEmpty()) {
                                            mainViewModel.deleteFolder(isDeletePermanently)
                                        }
                                    }
                                })
                            dialog.show(parentFragmentManager, Constants.DIALOG_DELETE)
                        }
                        // When click rename in menu
                        SetMenuFunction.RENAME -> {
                            val dialog = RenameDialog(setName = { isSet: Boolean, name: String ->
                                if (isSet) {
                                    mainViewModel.setName(newName = name, onLoading = {
                                        it.dialogLoading()
                                    }, onSuccess = {
                                        mainViewModel.clearFileState()
                                        mainViewModel.clearData()
                                        it.dialog?.dismiss()
                                        explorerAdapter.onRenameFile(name, position)
                                    }, onFail = {
                                        it.dialog?.dismiss()
                                        mainViewModel.clearFileState()
                                        mainViewModel.clearData()
                                    })
                                }
                            })
                            dialog.show(parentFragmentManager, Constants.DIALOG_RENAME)
                        }
                        // Check
                        SetMenuFunction.FAVORITE -> {
                            if (!it.selectedFileList[0].favorite) {
                                it.toast(getString(R.string.notify_add_favorite))
                            }
                            mainViewModel.handleFileFavorite(onLoading = { it.dialogLoading() },
                                onSuccess = {
                                    mainViewModel.getFavoriteFile(true)
                                    mainViewModel.clearFileState()
                                    mainViewModel.clearData()
                                    explorerAdapter.unSelectedPosition(position)
                                    mainViewModel.getFavoriteFile(false)
                                    it.dialog?.dismiss()
                                },
                                onDeleteSuccess = {
                                    mainViewModel.getFavoriteFile(true)
                                    mainViewModel.clearFileState()
                                    mainViewModel.clearData()
                                    it.dialog?.dismiss()
                                })
                        }

                        else -> {}
                    }
                }
            }
        }

        override fun onItemTypeAdapter(adapter: FilesAdapter?) {

        }
    }

    private fun initExplorerRecycler() {
        context?.let { context ->
            binding.rcvFiles.layoutManager = WrapContentLinearLayoutManager(context)
            binding.rcvFiles.adapter = explorerAdapter
            binding.rcvFiles.addItemDecoration(
                CommonItemDecoration(
                    Rect(
                        0,
                        context.dp2px(Constants.DP_4.toFloat()),
                        0,
                        context.dp2px(Constants.DP_4.toFloat())
                    )
                )
            )
            explorerAdapter.setOnClickEvent(onItemClick)
        }
    }

    private fun handleGetAllFile(state: State<Folder>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> binding.progressBar.isVisible = true
            State.DataStatus.SUCCESS -> {
                binding.progressBar.isVisible = false
                state.getData()?.let {
                    fileList.clear()
                    fileList.addAll(it.listFile)
                    explorerAdapter.setData(it.listFile.toMutableList())
                    binding.imgFileNotFound.isGone = it.listFile.isNotEmpty()
                    headerExplorerAdapter.setData(
                        ArrayList(viewModel.stackFolder.value?.toMutableList() ?: return)
                    )
                    binding.headerRecycler.smoothScrollToPosition(
                        binding.headerRecycler.layoutManager?.childCount ?: return
                    )
                }
            }

            State.DataStatus.ERROR -> binding.progressBar.isVisible = false
            else -> {}
        }
    }

    private fun handleStackChange(stateData: Stack<FileApp>?) {
        stateData?.let {
            if (it.isNotEmpty()) {
                mainViewModel.clearData()
                viewModel.getAllFileFromFolder(it.last().path)
            }
        }
    }

    private fun sortedByCallback(isAgreeSorted: Boolean, condition: String) {
        if (isAgreeSorted) {
            mainViewModel.sortedBy(condition)
            binding.toolbar.layoutMenu.isGone = true
        }
    }

    private fun sortedByForCondition(condition: String?) {
        condition?.let { con ->
            when (con) {
                Constants.SORTED_BY_NAME_FROM_A_TO_Z -> {
                    Timber.tag(myTag).d("sort by : $con")
                    fileList.sortBy { fileApp ->
                        fileApp.name
                    }
                }

                Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                    Timber.tag(myTag).d("sort by : $con")
                    fileList.sortByDescending { fileApp ->
                        fileApp.name
                    }
                }

                Constants.SORTED_BY_DATE -> {
                    Timber.tag(myTag).d("sort by : $con")
                    fileList.sortBy { fileApp ->
                        File(fileApp.path).lastModified()
                    }
                }

                Constants.SORTED_BY_SIZE -> {
                    try {
                        Timber.tag(myTag).d("sort by : $con")
                        fileList.sortBy { fileApp ->
                            fileApp.size
                        }
                    } catch (e: Exception) {
                        context?.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }

                Constants.SORTED_BY_TYPE_FROM_A_TO_Z -> {
                    try {
                        Timber.tag(myTag).d("sort by : $con")
                        fileList.sortBy { fileApp ->
                            fileApp.type
                        }
                    } catch (e: Exception) {
                        context?.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }

                Constants.SORTED_BY_TYPE_FROM_Z_TO_A -> {
                    try {
                        Timber.tag(myTag).d("sort by : $con")
                        fileList.sortByDescending { fileApp ->
                            fileApp.type
                        }
                    } catch (e: Exception) {
                        context?.toast(getString(R.string.notify_cannot_sort_apk))
                    }
                }

                else -> {}
            }
            if (con.isNotEmpty()) {
                explorerAdapter.setData(fileList)
            }
        }
    }

    private fun handleEventFromMain(multiSelect: MultiSelect?) {
        multiSelect?.let { select ->
            when (select) {
                is MultiSelect.SelectAll -> {
                    Timber.tag(myTag).d("MultiSelect.SelectAll")
                    explorerAdapter.multiSelect(true)
                    mainViewModel.multiSelectFile(fileList)
                }

                is MultiSelect.ClearAll -> {
                    Timber.tag(myTag).d("MultiSelect.ClearAll")
                    explorerAdapter.multiSelect(false)
                    mainViewModel.clearData()
                }

                is MultiSelect.Nothing -> {
                    Timber.tag(myTag).d("MultiSelect.Nothing")
                    if (explorerAdapter.isSelect) {
                        explorerAdapter.multiSelect(false)
                    }
                }
            }
        }
    }

    private fun handleOpenDirectory() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.openFolderStateFlow.collect { file ->
                    file?.let {
                        viewModel.addFolderToStack(file)
                        mainViewModel.closeDirectory()
                    }
                }
            }
        }
    }

    private fun setNameFolder(isAgree: Boolean, folderName: String) {
        if (isAgree) {
            try {
                File("${SingletonPath.getInstance().path}/$folderName").mkdirs()
                File(SingletonPath.getInstance().path).convertToFileApp()
                    ?.let { viewModel.addFolderToStack(it) }
                binding.toolbar.layoutMenu.isGone = true
            } catch (ex: IOException) {
                context?.toast(ex.message.toString())
            }
        }
    }

    override fun baseBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        if (SingletonMenu.getInstance().type != Constants.MENU_HANDLER && SingletonMenu.getInstance().type > -1) {
            mainViewModel.multiSelect(MultiSelect.ClearAll)
        } else {
            viewModel.stackFolder.value?.pop()
            if (viewModel.stackFolder.value?.isEmpty() == true) {
                (activity as MainActivity).let { act ->
                    act.removeFragment(this)
                    showInBaseNavigationView()
                }
            } else {
                viewModel.getAllFileFromFolder(viewModel.stackFolder.value?.last()?.path ?: return)
                SingletonPath.getInstance().path =
                    viewModel.stackFolder.value?.last()?.path.toString()
                mainViewModel.clearData()
                mainViewModel.multiSelect(MultiSelect.Nothing)
            }
        }
    }

    override fun onLayoutChange(type: LayoutType) {
        when (type) {
            LayoutType.LINEAR -> {
                context?.let { ct ->
                    binding.rcvFiles.adapter = null
                    binding.rcvFiles.layoutManager = WrapContentLinearLayoutManager(ct)
                    explorerAdapter.changeView(Constants.LIST_ITEM)
                    binding.rcvFiles.adapter = explorerAdapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.FILE_SPAN_COUNT)
                    binding.rcvFiles.adapter = null
                    binding.rcvFiles.layoutManager = layoutManager
                    explorerAdapter.changeView(Constants.GRID_ITEM)
                    binding.rcvFiles.adapter = explorerAdapter
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.toolbar.btnBack -> {
                onBackPressed()
            }

            binding.toolbar.imgOpenMenu -> {
                binding.toolbar.layoutMenu.isGone = !binding.toolbar.layoutMenu.isGone
            }

            binding.toolbar.tvCreateFolder -> {
                val diaLog =
                    ZipFileDialog(getString(R.string.create_folder), setName = ::setNameFolder)
                diaLog.show(parentFragmentManager, Constants.DIALOG_SORT_BY)
            }

            binding.toolbar.ctlView -> {
                binding.toolbar.ctlSwitch.visible()
                binding.toolbar.ctlView.gone()
            }

            binding.toolbar.fmDisplay -> {
                binding.toolbar.ctlSwitch.gone()
                binding.toolbar.ctlView.visible()
            }

            binding.toolbar.tvList -> {
                mainViewModel.changeLayoutLinear()
                binding.toolbar.layoutMenu.gone()
            }

            binding.toolbar.tvGrid -> {
                mainViewModel.changeLayoutGrid()
                binding.toolbar.layoutMenu.gone()
            }

            binding.toolbar.tvSortBy -> {
                val sortedDialog = SortedDialog(
                    screenType = PagerType.DOWNLOAD, sortedByCallback = ::sortedByCallback
                )
                sortedDialog.isCancelable = false
                sortedDialog.show(parentFragmentManager, Constants.DIALOG_SORT_BY)
            }

            else -> {}
        }
    }

    fun onPasteData(list: List<FileApp>) {
        mainViewModel.clearFileState()
        mainViewModel.clearData()
        explorerAdapter.onInsertFile(list)
    }

    fun onPasteFolderData(list: List<Folder>) {
        mainViewModel.clearFileState()
        mainViewModel.clearData()
        explorerAdapter.onInsertFile(viewModel.pasteFolder(list))
    }

    companion object {
        fun onSetupView(path: String): DetailsFolderFragment {
            val dialog = DetailsFolderFragment()
            dialog.folderPath = path
            return dialog
        }
    }
}