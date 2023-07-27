package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.AnalyticModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.FragmentDetailAnalyticBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.AnalyticDetailAdapter
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.DeleteDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.RenameDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AnalyticDetailFragment : BaseFragment(), View.OnClickListener {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentDetailAnalyticBinding
    private val analyticAdapter = AnalyticDetailAdapter()
    lateinit var analyticData: AnalyticModel
    private var numSelected: Int = 0
    private val listPositionSelect: MutableList<Int> = mutableListOf()
    private val onItemClick: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenFileScreen(file)
            }
        }

        override fun onSelectItem(file: FileApp, position: Int) {
            if (listPositionSelect.contains(position)) listPositionSelect.remove(position)
            else listPositionSelect.add(position)
            if (analyticData.title == Analytics.Apps.stringResId) {
                mainViewModel.onSelectApp(AppInstalled(
                    id = file.id,
                    packageName = file.path,
                    appName = file.name,
                    modified = file.dateModified,
                    size = file.size
                ).apply {
                    iconBitmap = file.iconBitmap
                    selected = file.isSelected
                })
            } else {
                mainViewModel.onSelectFile(file, position)
            }

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
                                                    analyticAdapter.onDeleteFile(
                                                        listPositionSelect
                                                    )
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
//                                        viewModel.updateMediaStore(name)
                                        analyticAdapter?.onRenameFile(name, position)
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
                                analyticAdapter?.unSelectedPosition(position)
                                it.dialog?.dismiss()
                            }, onDeleteSuccess = {
                                mainViewModel.getFavoriteFile(true)
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                analyticAdapter?.unSelectedPosition(position)
                                it.dialog?.dismiss()
                            })
                        }

                        SetMenuFunction.UNINSTALL -> {
                            it.uninstallApp()
                            it.onUninstallSS = {
                                analyticAdapter.onDeleteFile(position)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        override fun onItemTypeAdapter(adapter: FilesAdapter?) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailAnalyticBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
        return binding.root
    }

    override fun initUi() {
        super.initUi()
        initAdapter()
        binding.topAppBar.tvTitle.text = context?.getString(analyticData.title)
        binding.totalItem.text = convertSizeToString(analyticData.size)
        binding.topAppBar.tvCreateFolder.isGone = true
    }

    private fun convertSizeToString(size: Int): String = if (size == 0 || size == 1) {
        when (analyticData.title) {
            R.string.image -> "$size ${getString(R.string.image)}"
            R.string.screen_shot -> "$size ${getString(R.string.screen_shot)}"
            R.string.app -> "$size ${getString(R.string.app)}"
            R.string.music -> "$size ${getString(R.string.music)}"
            R.string.duplicate_file -> "$size ${getString(R.string.duplicate_file)}"
            R.string.larger_apps -> "$size ${getString(R.string.larger_app)}"
            else -> {
                ""
            }
        }
    } else {
        when (analyticData.title) {
            R.string.image -> "$size ${getString(R.string.images)}"
            R.string.screen_shot -> "$size ${getString(R.string.screen_shots)}"
            R.string.app -> "$size ${getString(R.string.apps)}"
            R.string.music -> "$size ${getString(R.string.musics)}"
            R.string.duplicate_file -> "$size ${getString(R.string.duplicate_files)}"
            R.string.larger_apps -> "$size ${getString(R.string.larger_apps)}"
            else -> {
                ""
            }
        }
    }


    override fun doWork() {
        initListener()
    }

    override fun onLayoutChange(type: LayoutType) {
        super.onLayoutChange(type)
        when (type) {
            LayoutType.LINEAR -> {
                context?.let { ct ->
                    binding.recycleData.adapter = null
                    binding.recycleData.layoutManager = WrapContentLinearLayoutManager(ct)
                    analyticAdapter?.changeView(Constants.LIST_ITEM)
                    binding.recycleData.adapter = analyticAdapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.APP_SPAN_COUNT)
                    binding.recycleData.adapter = null
                    binding.recycleData.layoutManager = layoutManager
                    analyticAdapter?.changeView(Constants.GRID_ITEM)
                    binding.recycleData.adapter = analyticAdapter
                }
            }
        }
    }

    private fun sortedByCallback(isAgreeSorted: Boolean, condition: String) {
        if (isAgreeSorted) {
            mainViewModel.sortedBy(condition)
            binding.topAppBar.layoutMenu.isGone = true
        }
    }

    override fun initListener() {
        binding.topAppBar.btnBack.setOnClickListener(this@AnalyticDetailFragment)
        binding.topAppBar.imgOpenMenu.setOnClickListener {
            binding.topAppBar.layoutMenu.isGone = !binding.topAppBar.layoutMenu.isGone
        }
        binding.topAppBar.tvSortBy.setOnClickListener {
            val sortedDialog =
                SortedDialog(screenType = PagerType.IMAGE, sortedByCallback = ::sortedByCallback)
            sortedDialog.isCancelable = true
            sortedDialog.show(parentFragmentManager, Constants.DIALOG_SORT_BY)
        }
        binding.topAppBar.ctlView.setOnClickListener {
            binding.topAppBar.ctlSwitch.visible()
            binding.topAppBar.ctlView.gone()
        }
        binding.topAppBar.fmDisplay.setOnClickListener {
            binding.topAppBar.ctlSwitch.gone()
            binding.topAppBar.ctlView.visible()
        }
        binding.topAppBar.tvList.setOnClickListener {
            mainViewModel.changeLayoutLinear()
            binding.topAppBar.layoutMenu.gone()
        }
        binding.topAppBar.tvGrid.setOnClickListener {
            mainViewModel.changeLayoutGrid()
            binding.topAppBar.layoutMenu.gone()
        }
        binding.topAppBar.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initAdapter() {
        doWithContext { context ->
            analyticAdapter.setDataList(analyticData.listFileApps)
            analyticAdapter.setOnClickEvent(callback = onItemClick)
            binding.apply {
                recycleData.layoutManager = WrapContentLinearLayoutManager(context = context)
                recycleData.adapter = analyticAdapter
            }
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
        numSelected = 0
        analyticData.listFileApps.map { it.isSelected = false }
        try {
            if (SingletonMenu.getInstance().type > -1) {
                mainViewModel.multiSelect(MultiSelect.ClearAll)
                SingletonMenu.getInstance().type = -1
            } else {
                (activity as MainActivity).let { act ->
                    act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    act.window.statusBarColor = ContextCompat.getColor(act, R.color.analytic_color)
                    act.supportFragmentManager.fragments.forEach { fragment ->
                        when (fragment) {
                            is PagerFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is DetailsFolderFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is AnalyticDetailFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }
//                            is DocumentFragment -> {
//                                act.removeFragment(fragment)
//                                return@forEach
//                            }
//                            is DropboxLoginFragment -> {
//                                act.removeFragment(fragment)
//                                return@forEach
//                            }
//                            is WifiTransferFragment -> {
//                                act.removeFragment(fragment)
//                                return@forEach
//                            }
//                            is RecentFragment -> {
//                                act.removeFragment(fragment)
//                                return@forEach
//                            }
//                            is FavoriteFragment -> {
//                                act.removeFragment(fragment)
//                                return@forEach
//                            }
                            else -> {}
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag(myTag).e("back pressed failed: ${e.message}")
        }
    }

    override fun onClick(v: View?) {
        when (view) {
            binding.topAppBar.btnBack -> {
                onBackPressed()
            }
        }
    }

    companion object {
        fun onSetupView(
            analytic: AnalyticModel
        ): AnalyticDetailFragment {
            val dialog = AnalyticDetailFragment()
            dialog.analyticData = analytic
            return dialog
        }
    }
}