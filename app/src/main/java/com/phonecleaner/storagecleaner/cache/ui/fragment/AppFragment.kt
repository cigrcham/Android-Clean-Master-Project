package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.FragmentListBinding
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.AppInstalledAdapter
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AppFragment : BaseFragment() {
    private val myTag:String = this::class.java.simpleName
    private lateinit var binding: FragmentListBinding
    private val viewModel: AppViewModel by viewModels()
    private val adapter = AppInstalledAdapter()
    private val appList: ArrayList<AppInstalled> = arrayListOf()
    var positionSelected: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initUi() {
        initSongRecycler()
    }

    val onItemClick: OnClickAppEvent = object : OnClickAppEvent {
        override fun onClickItem(item: AppInstalled, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenAppScreen(item)
            }
        }

        override fun onSelectItem(item: AppInstalled, position: Int) {
            mainViewModel.onSelectApp(item)
            SingletonMenu.getInstance().type = Constants.MENU_APP
            (activity as MainActivity).let { activity ->
                activity.cb = { data ->
                    when (data) {
                        SetMenuFunction.UNINSTALL -> {
                            activity.uninstallApp()
                            positionSelected = position
                        }

                        else -> {}
                    }
                }

                activity.onUninstallSS = {
                    adapter.onDeleteApp(position)
                }
            }
        }
    }

    private fun initSongRecycler() {
        doWithContext { context ->
            adapter.setOnClickEvent(onItemClick)
            binding.recycler.layoutManager = WrapContentLinearLayoutManager(context)
            binding.recycler.adapter = adapter
        }
    }

    override fun initData() {
        viewModel.getAppInstalled(getIconBitmap = true)
    }

    override fun doWork() {
        initObserve()
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listAppLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.isVisible = true
                    }

                    State.DataStatus.SUCCESS -> {
                        binding.progressBar.isVisible = false
                        appList.clear()
                        it.getData()?.let { data ->
                            adapter.setData(data)
                            appList.addAll(data)

                            mainViewModel.sortedByLiveData.observe(viewLifecycleOwner) { condition ->
                                when (condition) {
                                    Constants.SORTED_BY_NAME_FROM_A_TO_Z -> {
                                        data.sortBy { appInstalled ->
                                            appInstalled.appName
                                        }
                                    }

                                    Constants.SORTED_BY_NAME_FROM_Z_TO_A -> {
                                        data.sortByDescending { appInstalled ->
                                            appInstalled.appName
                                        }
                                    }

                                    Constants.SORTED_BY_DATE -> {
                                        data.sortBy { appInstalled ->
                                            appInstalled.modified
                                        }
                                    }

                                    Constants.SORTED_BY_SIZE -> {
                                        data.sortBy { appInstalled ->
                                            appInstalled.size
                                        }
                                    }

                                    else -> {}
                                }
                                if (condition.isNotEmpty()) {
                                    adapter.setData(data)
                                }
                            }
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
            observe(multiLiveData) { select ->
                when (select) {
                    is MultiSelect.SelectAll -> {
                        adapter.multiSelect(true)
                        mainViewModel.multiSelectApp(appList)
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
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = WrapContentLinearLayoutManager(ct)
                    adapter.changeView(Constants.LIST_ITEM)
                    binding.recycler.adapter = adapter
                }
            }

            LayoutType.GRID -> {
                context?.let { ct ->
                    val layoutManager = GridLayoutManager(ct, Constants.APP_SPAN_COUNT)
                    binding.recycler.adapter = null
                    binding.recycler.layoutManager = layoutManager
                    adapter.changeView(Constants.GRID_ITEM)
                    binding.recycler.adapter = adapter
                }
            }
        }
    }
}