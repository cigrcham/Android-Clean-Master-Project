package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.databinding.DialogFragmentAppSelectedBlockBinding
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.adapters.AppBlockAdapter
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.visible
import com.phonecleaner.storagecleaner.cache.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListAppBlockDialogFragment: DialogFragment() {
    private lateinit var binding: DialogFragmentAppSelectedBlockBinding
    private val viewModel by viewModels<AppViewModel>()
    var adapter: AppBlockAdapter? = null
    var listApp = arrayListOf<AppInstalled>()
    var listBlock = arrayListOf<AppInstalled>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullStatusDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentAppSelectedBlockBinding.inflate(layoutInflater, container, false)
        initOnclick()
        viewModel.getListAppBlock()
        viewModel.getAppInstalled()
        adapter = AppBlockAdapter { appInstalled, isCheck ->
            if (isCheck) {
                viewModel.insertDataBase(appInstalled)
            } else {
                viewModel.deleteDataBaseAppBlock(appInstalled)
            }
        }
        binding.rcvApp.adapter = adapter
        initObserve()
        return binding.root
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listAppLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                        binding.progressBar.visible()
                        binding.ctlHeader.gone()
                    }
                    State.DataStatus.SUCCESS -> {
                        it.getData()?.let { data ->
                            listApp.clear()
                            listApp.addAll(data)
                            adapter?.setData(data)
                            binding.tvCountApp.text = "${data.size} apps"
                        }
                        binding.progressBar.gone()
                        binding.ctlHeader.visible()
                    }
                    State.DataStatus.ERROR -> {
                        binding.progressBar.gone()
                        binding.tvCountApp.text = "0 app"
                    }
                    else -> {}
                }
            }

            observe(listAppBlock) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {

                    }
                    State.DataStatus.SUCCESS -> {
                        it.getData()?.let { data ->
                            adapter?.setListAppBlock(data)
                            listBlock.clear()
                            listBlock.addAll(data)
                        }
                    }
                    State.DataStatus.ERROR -> {

                    }
                    else -> {}
                }
            }
        }
    }

    private fun initOnclick() {
        binding.apply {
            imgBack.setOnClickListener {
                dismiss()
            }
            btnOnOff.setOnCheckedChangeListener { _, isCheck ->
                viewModel.selectAll(isCheck, listApp)
            }
        }
    }

    companion object {

        fun onSetupView(): ListAppBlockDialogFragment {
            val dialog = ListAppBlockDialogFragment()
            return dialog
        }
    }
}