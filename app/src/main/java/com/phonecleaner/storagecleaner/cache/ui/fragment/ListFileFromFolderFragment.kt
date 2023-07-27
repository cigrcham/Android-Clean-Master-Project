package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentListFileFromFolderBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.visible
import com.phonecleaner.storagecleaner.cache.viewmodel.ListFileFromFolderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ListFileFromFolderFragment : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentListFileFromFolderBinding
    private lateinit var folder: Folder
    private val listFileFromFolderViewModel: ListFileFromFolderViewModel by viewModels()
    private val listFile: ArrayList<FileApp> = arrayListOf()
    private val adapter = FilesAdapter()
    private var screenType = PagerType.IMAGE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListFileFromFolderBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun initData() {
        listFile.clear()
        folder.listFile.let { listFile.addAll(it) }
        if (listFile[0].isImage()) {
            screenType = PagerType.IMAGE
        }
        if (listFile[0].isAudio()) {
            screenType = PagerType.MUSIC
        }
        if (listFile[0].isVideo()) {
            screenType = PagerType.VIDEO
        }
        if (listFile[0].isApk()) {
            screenType = PagerType.APK
        }
        if (listFile[0].isZip()) {
            screenType = PagerType.ZIP
        }
    }

    override fun initUi() {
        context?.let {
            binding.toolbar.tvTitle.text = folder.name
            binding.imgFileNotFound.isGone = listFile.isNotEmpty()
            adapter.setOnClickEvent(onClickEvent)
            binding.rcvFiles.layoutManager = WrapContentLinearLayoutManager(it)
            binding.rcvFiles.adapter = adapter
            adapter.setData(listFile)
            binding.toolbar.tvCreateFolder.isGone = true
        }
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.toolbar.imgOpenMenu.setOnClickListener {
            binding.toolbar.layoutMenu.isGone = !binding.toolbar.layoutMenu.isGone
        }
        binding.toolbar.tvSortBy.setOnClickListener {
            val sortedDialog =
                SortedDialog(screenType = screenType, sortedByCallback = ::sortedByCallback)
            sortedDialog.isCancelable = false
            sortedDialog.show(parentFragmentManager, Constants.DIALOG_SORT_BY)
        }
        binding.toolbar.ctlView.setOnClickListener {
            binding.toolbar.ctlSwitch.visible()
            binding.toolbar.ctlView.gone()
        }
        binding.toolbar.fmDisplay.setOnClickListener {
            binding.toolbar.ctlSwitch.gone()
            binding.toolbar.ctlView.visible()
        }
        binding.toolbar.tvList.setOnClickListener {
            mainViewModel.changeLayoutLinear()
            binding.toolbar.layoutMenu.gone()
        }
        binding.toolbar.tvGrid.setOnClickListener {
            mainViewModel.changeLayoutGrid()
            binding.toolbar.layoutMenu.gone()
        }
    }

    override fun doWork() {
        initFileState()
        initObserver()
    }

    private fun initFileState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.fileStateFlow.collect { fileState ->
                    when (fileState) {
                        is FileState.START -> {
                            Timber.tag(myTag).d("FileState.START")
                        }

                        is FileState.PREPARE -> {
                            Timber.tag(myTag).d("FileState.PREPARE")
                        }

                        is FileState.LOADING -> {
                            Timber.tag(myTag).d("FileState.LOADING")
                        }

                        is FileState.SUCCESS -> {
                            Timber.tag(myTag).d("FileState.SUCCESS")
                            folder.path.let {
                                listFileFromFolderViewModel.getListFile(
                                    it, screenType
                                )
                            }
                            initListFileObserve()
                            mainViewModel.clearFileState()
                            mainViewModel.clearData()
                        }

                        is FileState.ERROR -> {
                            Timber.tag(myTag).d("FileState.ERROR")
                            mainViewModel.clearFileState()
                            mainViewModel.clearData()
                        }
                    }
                }
            }
        }
    }


    private fun initListFileObserve() {
        with(listFileFromFolderViewModel) {
            observe(listFileLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.isVisible = true
                    }

                    State.DataStatus.SUCCESS -> {
                        binding.progressBar.isGone = true
                        it.getData()?.let { data ->
                            listFile.clear()
                            listFile.addAll(data)
                            adapter.setData(data)
                        }
                    }

                    State.DataStatus.ERROR -> {
                        Timber.tag(myTag).e("load data failed: ${it.getStatus()}")
                        binding.progressBar.isGone = true
                    }

                    else -> {}
                }
            }
        }
    }

    private fun initObserver() {
        with(mainViewModel) {
            observe(sortedByLiveData) { condition ->
                condition?.let { con ->
                    when (con) {
                        Constants.SORTED_BY_NAME_FROM_A_TO_Z -> {
                            listFile.sortBy { file ->
                                file.name
                            }
                        }

                        Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                            listFile.sortByDescending { file ->
                                file.name
                            }
                        }

                        Constants.SORTED_BY_DATE -> {
                            listFile.sortBy { file ->
                                file.dateModified
                            }
                        }

                        Constants.SORTED_BY_SIZE -> {
                            listFile.sortBy { file ->
                                file.size
                            }
                        }

                        Constants.SORTED_BY_TYPE_FROM_A_TO_Z -> {
                            try {
                                Timber.tag(myTag).d("sort by : $con")
                                listFile.sortBy { fileApp ->
                                    fileApp.type
                                }
                            } catch (e: Exception) {
                                context?.toast(getString(R.string.notify_cannot_sort_apk))
                            }
                        }

                        Constants.SORTED_BY_TYPE_FROM_Z_TO_A -> {
                            try {
                                Timber.tag(myTag).d("sort by : $con")
                                listFile.sortByDescending { fileApp ->
                                    fileApp.type
                                }
                            } catch (e: Exception) {
                                context?.toast(getString(R.string.notify_cannot_sort_apk))
                            }
                        }

                        else -> {}
                    }
                    if (con.isNotEmpty()) {
                        adapter.setData(listFile)
                    }
                }
            }
            observe(multiLiveData) { select ->
                when (select) {
                    is MultiSelect.SelectAll -> {
                        adapter.multiSelect(true)
                        mainViewModel.multiSelectFile(listFile)
                    }

                    is MultiSelect.ClearAll -> {
                        adapter.multiSelect(false)
                        mainViewModel.clearData()
                    }

                    is MultiSelect.Nothing -> {
                        Timber.tag(myTag).d(("MultiSelect.Nothing"))
                        if (adapter.isSelect) {
                            adapter.multiSelect(false)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onLayoutChange(type: LayoutType) {
        when (type) {
            LayoutType.LINEAR -> {
                context?.let { ct ->
                    binding.rcvFiles.adapter = null
                    binding.rcvFiles.layoutManager = WrapContentLinearLayoutManager(ct)
                    adapter.changeView(Constants.LIST_ITEM)
                    binding.rcvFiles.adapter = adapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.FOLDER_SPAN_COUNT)
                    binding.rcvFiles.adapter = null
                    binding.rcvFiles.layoutManager = layoutManager
                    adapter.changeView(Constants.GRID_ITEM)
                    binding.rcvFiles.adapter = adapter
                }
            }
        }
    }

    private fun sortedByCallback(isAgreeSorted: Boolean, condition: String) {
        if (isAgreeSorted) {
            mainViewModel.sortedBy(condition)
            binding.toolbar.layoutMenu.isGone = true
        }
    }

    override fun baseBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
            (act as MainActivity).removeFragment(this)
        }
    }

    companion object {
        fun onSetupView(folder: Folder): ListFileFromFolderFragment {
            val dialog = ListFileFromFolderFragment()
            dialog.folder = folder
            return dialog
        }
    }
}