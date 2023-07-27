package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogNotifyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotifyDialog(
    private val title: String,
    private val acceptCallback: (Boolean) -> Unit = {}
) : DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogNotifyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogNotifyBinding.inflate(layoutInflater, container, false)
        initUi()
        initListener()
        return binding.root
    }

    private fun initUi() {
        when (title) {
            getString(R.string.reset_passcode) -> {
                binding.tvTitle.text = getString(R.string.reset_passcode)
                binding.tvContent.text = getString(R.string.notify_re_passcode)
            }
            getString(R.string.default_setting) -> {
                binding.tvTitle.text = getString(R.string.default_setting)
                binding.tvContent.text = getString(R.string.notify_default_setting)
            }
        }
    }

    private fun initListener() {
        binding.btnOk.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnOk -> {
                acceptCallback(true)
                dismiss()
            }
            binding.btnCancel -> {
                acceptCallback(false)
                dismiss()
            }
            else -> {}
        }
    }
}