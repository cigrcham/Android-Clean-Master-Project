package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.databinding.FragmentListFileRecoveryBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.RecoveryAdapter
import com.phonecleaner.storagecleaner.cache.viewmodel.FileRecoveryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFileRecoveryFragment : BaseFragment() {
    private lateinit var binding: FragmentListFileRecoveryBinding
    private val adapter = RecoveryAdapter()
    private val viewModel: FileRecoveryViewModel by navGraphViewModels(R.id.clean_graph_xml) {
        defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListFileRecoveryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun initUi() {
        super.initUi()
        binding.topAppBar.imgOpenMenu.isVisible = false
        binding.topAppBar.layoutMenu.isVisible = false
        doWithContext { context ->
            binding.apply {
                recycler.layoutManager = GridLayoutManager(context, 2)
                recycler.adapter = adapter
                adapter.onItemCLick = {
                    val bundle = Bundle()
                    bundle.putParcelable(KEY_FILE_RECOVERY, it)
                    findNavController().navigate(R.id.detailsRecoveryFileFragment, bundle)
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.fileRecoveryLiveData.observe(viewLifecycleOwner) {
            when (it.getStatus()) {
                State.DataStatus.LOADING -> {
                    (activity as MainActivity)?.dialogLoading()
                }

                State.DataStatus.SUCCESS -> {
                    (activity as MainActivity).dialogDismiss()
                    adapter.setData(it.getData() ?: arrayListOf())
                }

                State.DataStatus.ERROR -> {
                    (activity as MainActivity).dialogDismiss()
                    doWithContext { context ->
                        context.toast(getString(R.string.common_error))
                    }
                }

                else -> {}
            }
        }
    }

    override fun initData() {
        super.initData()
        initObservers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()

    }

    override fun initListener() {
        super.initListener()
        binding.topAppBar.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_listFileRecoveryFragment_to_coolerFragment)
        }
    }

    override fun baseBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_listFileRecoveryFragment_to_coolerFragment)
                }

            })
    }

    companion object {
        const val KEY_FILE_RECOVERY = "FILE_RECOVERY"
    }
}