package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentDetailsRecoveryBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FilterType
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.RecoveryDetailsAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.FilterBottomSheetDialog
import com.phonecleaner.storagecleaner.cache.viewmodel.DetailsRecoveryViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.FileRecoveryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsRecoveryFileFragment : BaseFragment() {
    private lateinit var binding: FragmentDetailsRecoveryBinding
    private var recoveryItem: Recovery? = null
    private var adapter = RecoveryDetailsAdapter()
    private var currentFilter = FilterType.ALL
    private val viewModel: DetailsRecoveryViewModel by viewModels()
    private val navGraphViewModel: FileRecoveryViewModel by viewModels()

    private var listSelectedFile = arrayListOf<FileApp>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsRecoveryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataBundle()
        initUi()
        initListener()
        initObserver()
    }

    private fun getDataBundle() {
        recoveryItem = arguments?.getParcelable(ListFileRecoveryFragment.KEY_FILE_RECOVERY)
        resetSelectedFile()
    }

    private fun resetSelectedFile() {
        recoveryItem?.listFile?.onEach { it.isSelected = false }
    }

    @SuppressLint("StringFormatInvalid")
    override fun initUi() {
        binding.tvTitle.text = recoveryItem?.name

        val layoutManager = GridLayoutManager(context, SPAN_COUNT)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (adapter.listItemByDate[position] is String) {
                    return SPAN_COUNT
                } else {
                    return 1
                }
            }
        }
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter
        recoveryItem?.let {
            adapter.setData(it.listFile, currentFilter)
        }
        adapter.onItemSelectedListener = {
            listSelectedFile = it
            if (it.size > 0) {
                binding.btnRecover.isVisible = true
                binding.tvTitle.text = context?.getString(R.string.select, it.size)
            } else {
                binding.btnRecover.isVisible = false
                binding.tvTitle.text = context?.getString(R.string.file_recovery)
            }
        }
    }

    override fun initListener() {
        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.imgHistory.setOnClickListener {
            val bundle = bundleOf(ListFileRecoveryFragment.KEY_FILE_RECOVERY to recoveryItem)
            findNavController().navigate(R.id.recoveryHistoryFragment, bundle)
        }

        binding.imgFilter.setOnClickListener {
            showBottomSheet()
        }

        binding.btnRecover.setOnClickListener {
            recoveryItem?.let { recoveryItem ->
                viewModel.restoreFile(recoveryItem, listSelectedFile)
            }
        }
    }

    private fun initObserver() {
        viewModel.restoreFileStateLiveData.observe(viewLifecycleOwner) {
            when (it.getStatus()) {
                State.DataStatus.LOADING -> {
                    (activity as? MainActivity)?.dialogLoading()
                }

                State.DataStatus.SUCCESS -> {
                    (activity as? MainActivity)?.dialog?.dismiss()
                    recoveryItem?.let {
                        it.listFile.removeAll(listSelectedFile)
                        adapter.setData(it.listFile, currentFilter)
                        resetState()
                        //reload data
                        navGraphViewModel.loadFileRecovery()
                    }

                    doWithContext {
                        it.toast(it.getString(R.string.recovery_success))
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

    override fun baseBackPressed() {
        super.baseBackPressed()
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    private fun resetState() {
        binding.btnRecover.isVisible = false
        binding.tvTitle.text = context?.getString(R.string.file_recovery)
    }

    private fun showBottomSheet() {
        val dialog = FilterBottomSheetDialog(currentFilter) { type ->
            this.currentFilter = type
            recoveryItem?.let {
                resetSelectedFile()
                resetState()
                adapter.setData(it.listFile, currentFilter)
            }
        }
        dialog.show(parentFragmentManager, "")
    }

    companion object {
        const val SPAN_COUNT = 4
    }
}