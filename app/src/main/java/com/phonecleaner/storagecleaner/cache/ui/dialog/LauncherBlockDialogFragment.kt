package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseDialog
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import com.phonecleaner.storagecleaner.cache.data.model.response.MessageNotificationState
import com.phonecleaner.storagecleaner.cache.databinding.DialogLauncherBlockNotificationBinding
import com.phonecleaner.storagecleaner.cache.ui.adapters.NotificationAppAdapter
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.showDialog
import com.phonecleaner.storagecleaner.cache.utils.visible
import com.phonecleaner.storagecleaner.cache.viewmodel.MessageActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LauncherBlockDialogFragment : BaseDialog() {
    private lateinit var binding: DialogLauncherBlockNotificationBinding
    private val viewModel: MessageActivityViewModel by viewModels()
    private var adapter: NotificationAppAdapter? = null
    private var listItemSelected: ArrayList<MessageNotifi> = arrayListOf()
    private var listItem: ArrayList<MessageNotifi> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFile()
        setStyle(STYLE_NORMAL, R.style.FullStatusDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogLauncherBlockNotificationBinding.inflate(layoutInflater, container, false)
        adapter = NotificationAppAdapter(onItemClick = {
            if (!listItemSelected.contains(it)) {
                listItemSelected.add(it)
            } else {
                listItemSelected.remove(it)
            }
        })
        binding.rcvMessage.adapter = adapter
        initOnclick()
        observeViewModel()
        return binding.root
    }

    private fun initOnclick() {
        binding.apply {
            imgBack.setOnClickListener {
                dismiss()
            }
            imgBlockApp.setOnClickListener {
                activity?.let { ac ->
                    val dialog = ListAppBlockDialogFragment.onSetupView()
                    ac.showDialog(dialog, parentFragmentManager)
                }
            }

            btnBlock.setOnClickListener {
                viewModel.cancelNotification(listItemSelected)
                listItemSelected.clear()
                binding.cbSelectAll.isChecked = false
                adapter?.selectAllItem(false)
            }

            cbSelectAll.setOnCheckedChangeListener { _, isCheck ->
                adapter?.selectAllItem(isCheck)
                if (isCheck) {
                    listItemSelected.clear()
                    listItemSelected.addAll(listItem)
                } else {
                    listItemSelected.clear()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listMessageNotification.collect { message ->
                    when (message) {
                        is MessageNotificationState.Error -> {
                            binding.tvCountMessage.text = "0 messages"
                            binding.cvHeader.gone()
                        }

                        is MessageNotificationState.Success -> {
                            if (message.listFile.isNotEmpty()) {
                                adapter?.setData(message.listFile)
                                listItem.addAll(message.listFile)
                                binding.tvCountMessage.text = "${message.listFile.size} messages"
                                binding.cbSelectAll.isChecked = false
                                binding.cvHeader.visible()
                            } else {
                                adapter?.setData(arrayListOf())
                                binding.tvCountMessage.text = "0 messages"
                                binding.cvHeader.gone()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun onSetupView(): LauncherBlockDialogFragment {
            return LauncherBlockDialogFragment()
        }
    }
}