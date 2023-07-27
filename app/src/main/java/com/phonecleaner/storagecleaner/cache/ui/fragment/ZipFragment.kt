package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.graphics.Rect
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.FragmentZipBinding
import com.phonecleaner.storagecleaner.cache.extension.dp2px
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.adapters.HeaderExplorerAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.CommonItemDecoration
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.visible
import com.phonecleaner.storagecleaner.cache.viewmodel.ZipFileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ZipFragment : BaseFragment(), View.OnClickListener {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentZipBinding
    private val viewModel: ZipFileViewModel by viewModels()
    private val headerExplorerAdapter = HeaderExplorerAdapter()
    private val adapter = FilesAdapter()
    private val listFile: ArrayList<FileApp> = arrayListOf()
    private val listPositionSelect: MutableList<Int> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentZipBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
        return binding.root
    }

    override fun initUi() {
        initExplorerRecycler()
        initHeaderExplorerRecycler()
        binding.toolbar.tvTitle.text = getString(R.string.zip_files)
        binding.toolbar.tvCreateFolder.isGone = true
    }

    override fun initData() {
        viewModel.getListFileZip()
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener(this)
        binding.toolbar.imgOpenMenu.setOnClickListener(this)
        binding.toolbar.tvSortBy.setOnClickListener(this)
        binding.toolbar.ctlView.setOnClickListener(this)
        binding.toolbar.fmDisplay.setOnClickListener(this)
        binding.toolbar.tvList.setOnClickListener(this)
        binding.toolbar.tvGrid.setOnClickListener(this)
    }

    override fun doWork() {
        initFileState()
        initObserve()
    }

    private fun initHeaderExplorerRecycler() {
        context?.let {
            binding.headerRecycler.layoutManager =
                WrapContentLinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
            binding.headerRecycler.adapter = headerExplorerAdapter
            headerExplorerAdapter.setData(arrayListOf(FileApp(name = getString(R.string.zip_files))))
        }
    }

    val onItemClick: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenFileScreen(file)
            }
        }

        override fun onSelectItem(file: FileApp, position: Int) {
            mainViewModel.onSelectFile(file, position)
            if (listPositionSelect.contains(position)) {
                listPositionSelect.remove(position)
            } else {
                listPositionSelect.add(position)
            }
            SingletonMenu.getInstance().type = Constants.MENU_NORMAL
            activity?.let {
                (it as MainActivity).cb = { data ->
                    Timber.d("TAG345 vao $data")
                    when (data) {
                        SetMenuFunction.DELETE -> {
                            val dialog =
                                DeleteDialog(getStateDelete = { isDeletePermanently: Boolean, isDelete: Boolean ->
                                    if (isDelete) {
                                        if (it.selectedFileList.isNotEmpty()) {
                                            mainViewModel.deleteFile(isDeletePermanently,
                                                onLoading = {
                                                    it.dialogLoading()
                                                },
                                                onSuccess = {
                                                    mainViewModel.clearFileState()
                                                    mainViewModel.clearData()
                                                    it.dialog?.dismiss()
                                                    adapter.onDeleteFile(listPositionSelect)
                                                },
                                                onFail = {
                                                    it.dialog?.dismiss()
                                                    mainViewModel.clearFileState()
                                                    mainViewModel.clearData()
                                                    listPositionSelect.clear()
                                                })
                                        } else if (it.selectedFolderList.isEmpty()) {
                                            mainViewModel.deleteFolder(isDeletePermanently)
                                        }
                                    }
                                })
                            dialog.show(parentFragmentManager, Constants.DIALOG_DELETE)
                        }

                        SetMenuFunction.RENAME -> {
                            val dialog = RenameDialog(setName = { isSet: Boolean, name: String ->
                                if (isSet) {
                                    mainViewModel.setName(newName = name, onLoading = {
                                        it.dialogLoading()
                                    }, onSuccess = {
                                        mainViewModel.clearFileState()
                                        mainViewModel.clearData()
                                        it.dialog?.dismiss()
                                        adapter.onRenameFile(name, position)
                                    }, onFail = {
                                        it.dialog?.dismiss()
                                        mainViewModel.clearFileState()
                                        mainViewModel.clearData()
                                    })
                                }
                            })
                            dialog.show(parentFragmentManager, Constants.DIALOG_RENAME)
                        }

                        SetMenuFunction.FAVORITE -> {
                            if (!it.selectedFileList[0].favorite) {
                                it.toast(getString(R.string.notify_add_favorite))
                            }
                            mainViewModel.handleFileFavorite(onLoading = {
                                it.dialogLoading()
                            }, onSuccess = {
                                mainViewModel.getFavoriteFile(true)
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                adapter.unSelectedPosition(position)
                                it.dialog?.dismiss()
                            }, onDeleteSuccess = {
                                mainViewModel.getFavoriteFile(true)
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                adapter.unSelectedPosition(position)
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
        doWithContext { context ->
            binding.rcvFiles.layoutManager = WrapContentLinearLayoutManager(context)
            binding.rcvFiles.adapter = adapter
            binding.rcvFiles.addItemDecoration(
                CommonItemDecoration(
                    Rect(
                        0, context.dp2px(Constants.DP_4.toFloat()), 0, context.dp2px(
                            Constants.DP_4.toFloat()
                        )
                    )
                )
            )
            adapter.setOnClickEvent(onItemClick)
        }
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
                            viewModel.getListFileZip()
                            mainViewModel.clearFileState()
                            mainViewModel.clearData()
                            binding.progressBar.isVisible = true
                            delay(4000)
                            binding.progressBar.isGone = true
                        }

                        is FileState.ERROR -> {
                            Timber.tag(myTag).d("FileState.ERROR")
                            mainViewModel.clearFileState()
                            mainViewModel.clearData()
                            binding.progressBar.isVisible = true
                            delay(4000)
                            binding.progressBar.isGone = true
                        }
                    }
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.listFileZipLiveData.observe(this) {
            when (it?.getStatus()) {
                State.DataStatus.LOADING -> {
                    binding.progressBar.isVisible = true
                }

                State.DataStatus.SUCCESS -> {
                    binding.progressBar.isVisible = false
                    it.getData()?.let { data ->
                        adapter.setData(data)
                        listFile.clear()
                        listFile.addAll(data)
                    }
                }

                State.DataStatus.ERROR -> {
                    binding.progressBar.isVisible = true
                }

                else -> {}
            }
        }
        mainViewModel.sortedByLiveData.observe(viewLifecycleOwner) { condition ->
            when (condition) {
                Constants.SORTED_BY_NAME_FROM_A_TO_Z -> {
                    listFile.sortBy { fileApp ->
                        fileApp.name
                    }
                }

                Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                    listFile.sortByDescending { fileApp ->
                        fileApp.name
                    }
                }

                Constants.SORTED_BY_DATE -> {
                    listFile.sortBy { fileApp ->
                        fileApp.dateModified
                    }
                }

                Constants.SORTED_BY_SIZE -> {
                    listFile.sortBy { fileApp ->
                        fileApp.size
                    }
                }

                Constants.SORTED_BY_TYPE_FROM_A_TO_Z -> {
                    context?.toast(getString(R.string.notify_cannot_sort_album))
                }

                Constants.SORTED_BY_TYPE_FROM_Z_TO_A -> {
                    context?.toast(getString(R.string.notify_cannot_sort_album))
                }

                else -> {}
            }
            if (condition.isNotEmpty()) {
                adapter.setData(listFile)
            }
        }
        mainViewModel.multiLiveData.observe(viewLifecycleOwner) { select ->
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
                    if (adapter.isSelect) {
                        adapter.multiSelect(false)
                    }
                }

                else -> {}
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
                    val layoutManager = GridLayoutManager(ct, Constants.FILE_SPAN_COUNT)
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
            (act as MainActivity).removeFragment(this)
            showInBaseNavigationView()
        }
    }

    companion object {
        fun onSetupView(): ZipFragment {
            return ZipFragment()
        }
    }
}