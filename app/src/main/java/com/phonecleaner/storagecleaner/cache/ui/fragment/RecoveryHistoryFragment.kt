package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.databinding.FragmentRecoveryHistoryBinding
import com.phonecleaner.storagecleaner.cache.base.BaseCacheFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FilterType
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.RecoveryDetailsAdapter
import com.phonecleaner.storagecleaner.cache.viewmodel.DetailsRecoveryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoveryHistoryFragment : BaseCacheFragment<FragmentRecoveryHistoryBinding>() {
    private var recoveryItem: Recovery? = null
    private val viewModel: DetailsRecoveryViewModel by viewModels()
    private var adapter = RecoveryDetailsAdapter(false)

    override fun createView(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecoveryHistoryBinding {
        return FragmentRecoveryHistoryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataBundle()
        getHistory()
        initListener()
        initObservers()
        initUi()
    }

    private fun getDataBundle() {
        recoveryItem = arguments?.getParcelable(ListFileRecoveryFragment.KEY_FILE_RECOVERY)
    }

    private fun getHistory() {
        recoveryItem?.let {
            viewModel.getRecoverHistory(it)
        }
    }

    private fun initObservers() {
        viewModel.recoverHistoryLiveData.observe(viewLifecycleOwner) {
            when (it.getStatus()) {
                State.DataStatus.LOADING -> {
                    (activity as? MainActivity)?.dialogLoading()
                }

                State.DataStatus.SUCCESS -> {
                    (activity as? MainActivity)?.dialog?.dismiss()
                    it.getData()?.let {
                        adapter.setData(it, FilterType.ALL)
                    }
                }

                State.DataStatus.ERROR -> {
                    (activity as? MainActivity)?.dialog?.dismiss()
                    it.getErrorMsg()?.let {
                        context?.toast(it)
                    }
                }

                else -> {}
            }
        }
    }

    override fun initUi() {
        val layoutManager = GridLayoutManager(context, DetailsRecoveryFileFragment.SPAN_COUNT)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (adapter.listItemByDate[position] is String) {
                    return DetailsRecoveryFileFragment.SPAN_COUNT
                } else {
                    return 1
                }
            }
        }
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter

    }

    override fun initListener() {
        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}