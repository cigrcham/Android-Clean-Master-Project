package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogLogoutBinding

class LogoutDialog(
    private var getLogoutState: (Boolean) -> Unit = {}
) : DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogLogoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLogoutBinding.inflate(layoutInflater, container, false)
        isCancelable = false
        initListener()
        return binding.root
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
                getLogoutState(true)
                dismiss()
            }
            binding.btnCancel -> {
                getLogoutState(false)
                dismiss()
            }
            else -> {}
        }
    }
}