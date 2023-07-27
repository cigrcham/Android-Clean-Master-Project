package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.phonecleaner.storagecleaner.cache.databinding.FragmentTabContainerBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.adapters.RecentTabAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.AddFavoriteDialog
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentTabFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentTabContainerBinding
    private val fileList: ArrayList<FileApp> = arrayListOf()
    private val recentAdapter = RecentTabAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabContainerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun doWork() {
        super.doWork()
        initObserve()
        initFileState()
    }

    override fun initUi() {
        super.initUi()
        setupRecycleRecent()
    }

    override fun initData() {
        super.initData()
        mainViewModel.getRecentFile(limit = 30)
    }

    override fun initListener() {
        super.initListener()
    }

    override fun onClick(v: View?) {
    }

    private fun initFileState() {

    }

    private fun initObserve() {
        with(mainViewModel) {
            observe(recentFileLiveData, ::getData)
        }
    }

    private fun getData(stateData: State<List<FileApp>>?) {
        when (stateData?.getStatus()) {
            State.DataStatus.LOADING -> {
                binding.progressBar.isVisible = true
            }

            State.DataStatus.SUCCESS -> {
                binding.progressBar.isGone = true
                stateData.getData()?.let {
                    fileList.clear()
                    fileList.addAll(it)
                    recentAdapter.setFileAppLists(fileList)
                }
            }

            State.DataStatus.ERROR -> {
                binding.progressBar.isGone = true
            }

            else -> {}
        }
    }

    override val onClickEvent: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            val dialog = AddFavoriteDialog.onSetUpView(file, true)
            dialog.show(parentFragmentManager, AddFavoriteDialog::class.java.simpleName)
        }

        override fun onSelectItem(file: FileApp, position: Int) {
        }

        override fun onItemTypeAdapter(adapter: FilesAdapter?) {
        }
    }

    private fun setupRecycleRecent() {
        doWithContext { context ->
            binding.recycleRecent.adapter = recentAdapter
            binding.recycleRecent.layoutManager = WrapContentLinearLayoutManager(context)
            recentAdapter.setOnClickEvent(callback = onClickEvent)
        }
    }
}