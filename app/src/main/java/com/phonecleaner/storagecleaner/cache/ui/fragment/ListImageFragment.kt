package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentListBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MediaStoreState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.extension.managerStoragePermissionGranted
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ListImageFragment : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentListBinding
    private val viewModel: ImageViewModel by viewModels()
    private var adapter: FilesAdapter? = null
    private val listFile: ArrayList<FileApp> = arrayListOf()
    private val listPositionSelect: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initUi() {
        initImageRecycler()
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
                                                    adapter?.onDeleteFile(listPositionSelect)
                                                    listPositionSelect.clear()
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
                                        viewModel.updateMediaStore(name)
                                        adapter?.onRenameFile(name, position)
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
                                adapter?.unSelectedPosition(position)
                                it.dialog?.dismiss()
                            }, onDeleteSuccess = {
                                mainViewModel.getFavoriteFile(true)
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                adapter?.unSelectedPosition(position)
                                it.dialog?.dismiss()
                            })
                        }

                        SetMenuFunction.HIDE -> {
                            mainViewModel.hideFile(onLoading = {
                                it.dialogLoading()
                            }, onSuccess = {
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                adapter?.onDeleteFile(listPositionSelect)
                                listPositionSelect.clear()
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

    private fun initImageRecycler() {
        doWithContext { context ->
            adapter = FilesAdapter()
            adapter?.setOnClickEvent(onItemClick)
            binding.recycler.layoutManager = WrapContentLinearLayoutManager(context)
            binding.recycler.adapter = adapter
        }
    }

    override fun initData() {
        activity?.let {
            if (it.managerStoragePermissionGranted()) {
                viewModel.getFileImage()
            }
            viewModel.getFileImage()
        }
    }

    override fun doWork() {
        initFileState()
        initMediaStoreState()
        initObserve()
    }

    private fun initFileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.fileStateFlow.collect { fileState ->
                when (fileState) {
                    is FileState.START -> {
                        Timber.tag(myTag).d("FileState.START")
                    }

                    is FileState.PREPARE -> {}
                    is FileState.LOADING -> {
                        Timber.tag(myTag).d("FileState.LOADING")
                    }

                    is FileState.SUCCESS -> {
                        Timber.tag(myTag).d("FileState.SUCCESS")
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

    private fun initMediaStoreState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.mediaStoreStateFlow.collect { mediaStoreState ->
                    when (mediaStoreState) {
                        is MediaStoreState.NOTHING -> {}
                        is MediaStoreState.INSERT -> {
                            viewModel.insertMediaStore()
                            mainViewModel.clearMediaStoreState()
                        }

                        is MediaStoreState.UPDATE -> {
                            viewModel.updateMediaStore("")
                            mainViewModel.clearMediaStoreState()
                        }

                        is MediaStoreState.DELETE -> {

                        }
                    }
                }
            }
        }
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listImageLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.isVisible = true
                    }

                    State.DataStatus.SUCCESS -> {
                        binding.progressBar.isVisible = false
                        it.getData()?.let { listImage ->
                            adapter?.setData(listImage)
                            listFile.clear()
                            listFile.addAll(listImage)
                        }
                    }

                    State.DataStatus.ERROR -> {
                        binding.progressBar.isVisible = true
                    }

                    else -> {}
                }
            }
        }
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
                    if (condition.isNotEmpty()) {
                        adapter?.setData(listFile)
                    }
                }
            }
            observe(multiLiveData) { select ->
                when (select) {
                    is MultiSelect.SelectAll -> {
                        listFile.forEach { fileApp ->
                            fileApp.isSelected = true
                        }
                        adapter?.setData(listFile)
                        mainViewModel.multiSelectFile(listFile)
                    }

                    is MultiSelect.ClearAll -> {
                        adapter?.multiSelect(false)
                        listFile.forEach { fileApp ->
                            fileApp.isSelected = false
                        }
                        adapter?.setData(listFile)
                        mainViewModel.clearData()
                    }

                    is MultiSelect.Nothing -> {
                        Timber.tag(myTag).d("MultiSelect.Nothing")
                    }

                    else -> {}
                }
            }

            observe(selectedFileLiveData) {

            }
        }
    }

    override fun onLayoutChange(type: LayoutType) {
        when (type) {
            LayoutType.LINEAR -> {
                context?.let { ct ->
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = WrapContentLinearLayoutManager(ct)
                    adapter?.changeView(Constants.LIST_ITEM)
                    binding.recycler.adapter = adapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.FILE_SPAN_COUNT)
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = layoutManager
                    adapter?.changeView(Constants.GRID_ITEM)
                    binding.recycler.adapter = adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag(myTag).d("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(myTag).d("onDestroy")
    }
}