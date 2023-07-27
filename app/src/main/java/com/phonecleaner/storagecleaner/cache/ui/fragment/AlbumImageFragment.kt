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
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MediaStoreState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.FragmentListBinding
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.AlbumAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class AlbumImageFragment : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentListBinding
    private val viewModel: ImageViewModel by viewModels()
    private var adapter = AlbumAdapter()
    private val fileList: ArrayList<Folder> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initUi() {
        initImageRecycler()
    }

    val onItemClick: OnClickFolderEvent = object : OnClickFolderEvent {
        override fun onClickItem(folder: Folder, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenFolderScreen(folder)
            }
        }

        override fun onSelectItem(folder: Folder, position: Int) {
            mainViewModel.onSelectFolder(folder)
            SingletonMenu.getInstance().type = Constants.MENU_NORMAL
            activity?.let {
                (it as MainActivity).cb = { data ->
                    when (data) {
                        SetMenuFunction.DELETE -> {
                            val dialog =
                                DeleteDialog(getStateDelete = { isDeletePermanently: Boolean, isDelete: Boolean ->
                                    if (isDelete) {
                                        if (it.selectedFolderList.isNotEmpty()) {
                                            mainViewModel.deleteFolder(
                                                isDeletePermanently,
                                                onLoading = {
                                                    it.dialogLoading()
                                                },
                                                onSuccess = {
                                                    mainViewModel.clearFileState()
                                                    mainViewModel.clearData()
                                                    it.dialog?.dismiss()
                                                    adapter.onDeleteFolder(position)
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

                        else -> {}
                    }
                }
            }
        }
    }

    private fun initImageRecycler() {
        doWithContext { context ->
            adapter.setOnClickEvent(onItemClick)
            binding.recycler.layoutManager = WrapContentLinearLayoutManager(context)
            binding.recycler.adapter = adapter
        }
    }

    override fun initData() {
        viewModel.getAlbumImage()
    }

    override fun doWork() {
        initObserve()
        initFileState()
        initMediaStoreState()
    }

    private fun initFileState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.fileStateFlow.collect { fileState ->
                    when (fileState) {
                        is FileState.START -> {}
                        is FileState.PREPARE -> {}
                        is FileState.LOADING -> {}
                        is FileState.SUCCESS -> {}
                        is FileState.ERROR -> {}
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
                            viewModel.removeMediaStore()
                            mainViewModel.clearMediaStoreState()
                        }
                    }
                }
            }
        }
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listAlbumLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.isVisible = true
                    }

                    State.DataStatus.SUCCESS -> {
                        binding.progressBar.isVisible = false
                        adapter.setData(it.getData() ?: return@observe)
                        it.getData()?.let { list ->
                            fileList.clear()
                            fileList.addAll(list)
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
                            fileList.sortBy { folder ->
                                folder.name
                            }
                        }

                        Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                            fileList.sortByDescending { folder ->
                                folder.name
                            }
                        }

                        Constants.SORTED_BY_DATE -> {
                            fileList.sortBy { folder ->
                                File(folder.path).lastModified()
                            }
                        }

                        Constants.SORTED_BY_SIZE -> {
                            context?.toast(getString(R.string.notify_cannot_sort_album))
                        }

                        Constants.SORTED_BY_TYPE_FROM_A_TO_Z -> {
                            context?.toast(getString(R.string.notify_cannot_sort_album))
                        }

                        Constants.SORTED_BY_TYPE_FROM_Z_TO_A -> {
                            context?.toast(getString(R.string.notify_cannot_sort_album))
                        }

                        else -> {}
                    }
                    if (con.isNotEmpty()) {
                        adapter.setData(fileList)
                    }
                }
            }
            observe(multiLiveData) { select ->
                when (select) {
                    is MultiSelect.SelectAll -> {
                        adapter.multiSelect(true)
                        mainViewModel.multiSelectFolder(fileList)
                    }

                    is MultiSelect.ClearAll -> {
                        adapter.multiSelect(false)
//                        mainViewModel.clearData()
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
    }

    override fun onLayoutChange(type: LayoutType) {
        when (type) {
            LayoutType.LINEAR -> {
                context?.let { ct ->
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = WrapContentLinearLayoutManager(ct)
                    adapter.changeView(Constants.LIST_ITEM)
                    binding.recycler.adapter = adapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.FOLDER_SPAN_COUNT)
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = layoutManager
                    adapter.changeView(Constants.GRID_ITEM)
                    binding.recycler.adapter = adapter
                }
            }
        }
    }
}