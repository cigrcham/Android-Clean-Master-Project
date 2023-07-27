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
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
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
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.AudioViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class AudioFragment : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentListBinding
    private val viewModel: AudioViewModel by viewModels()
    private val adapter: FilesAdapter = FilesAdapter()
    private val fileList: ArrayList<FileApp> = arrayListOf()
    private val listPositionSelect: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initUi() {
        initSongRecycler()
    }

    val onItemClick: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            activity?.let { act ->
                (act as MainActivity).navigateToOpenFileScreen(file)
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
                                                    adapter.onDeleteFile(listPositionSelect)
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
                                        viewModel.updateMediaStore(name)
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

                        SetMenuFunction.HIDE -> {
                            mainViewModel.hideFile(onLoading = {
                                it.dialogLoading()
                            }, onSuccess = {
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                adapter.onDeleteFile(listPositionSelect)
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

    /**
     * Init recycle list view adapter
     */
    private fun initSongRecycler() {
        doWithContext { context ->
            adapter.setOnClickEvent(onItemClick)
            binding.recycler.layoutManager = WrapContentLinearLayoutManager(context)
            binding.recycler.adapter = adapter
        }
    }

    /**
     * Call function get data in viewModel
     */
    override fun initData() {
        viewModel.getAudio()
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
                            Timber.tag(myTag).d("FileState.Error")
                            mainViewModel.clearFileState()
                            mainViewModel.clearData()
                        }
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
                        is MediaStoreState.NOTHING -> {
                        }

                        is MediaStoreState.INSERT -> {
                            viewModel.insertMediaStore()
                            mainViewModel.clearMediaStoreState()
                        }

                        is MediaStoreState.UPDATE -> {
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
                    val layoutManager = GridLayoutManager(ct, Constants.FILE_SPAN_COUNT)
                    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when {
                                position == 0 -> Constants.FILE_SPAN_COUNT
                                position % 10 == 0 -> Constants.FILE_SPAN_COUNT
                                else -> 1
                            }
                        }

                    }
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = layoutManager
                    adapter.changeView(Constants.GRID_ITEM)
                    binding.recycler.adapter = adapter
                }
            }
        }
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listAudioLiveData) { state ->
                when (state?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.isVisible = true
                    }

                    State.DataStatus.SUCCESS -> {
                        binding.progressBar.isVisible = false
                        state.getData()?.let { listSongFolder ->
                            adapter.setData(
                                listSongFolder.map { it.listFile }.flatten().toMutableList()
                            )
                            fileList.clear()
                            fileList.addAll(
                                listSongFolder.map { it.listFile }.flatten().toMutableList()
                            )
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
                            fileList.sortBy { fileApp ->
                                fileApp.name
                            }
                        }

                        Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                            fileList.sortByDescending { fileApp ->
                                fileApp.name
                            }
                        }

                        Constants.SORTED_BY_DATE -> {
                            fileList.sortBy { fileApp ->
                                fileApp.dateModified
                            }
                        }

                        Constants.SORTED_BY_SIZE -> {
                            fileList.sortBy { fileApp ->
                                fileApp.size
                            }
                        }

                        Constants.SORTED_BY_TYPE_FROM_A_TO_Z -> {
//                            context?.toast(getString(R.string.notify_cannot_sort_apk))
                        }

                        Constants.SORTED_BY_TYPE_FROM_Z_TO_A -> {
//                            context?.toast(getString(R.string.notify_cannot_sort_apk))
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
                        mainViewModel.multiSelectFile(fileList)
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
}
