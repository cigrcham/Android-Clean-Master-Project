package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogSetupPasscodeBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupPasscodeDialog(
    private val setupPasscode: (String) -> Unit = {}
) : DialogFragment() {
    private lateinit var binding: DialogSetupPasscodeBinding
    private var passcode = ""
    private var rePasscode = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogSetupPasscodeBinding.inflate(layoutInflater, container, false)
        initListener()
        return binding.root
    }

    private fun checkPasscode(): Int {
        passcode = binding.editPassword.text.toString()
        rePasscode = binding.editRePasscode.text.toString()
        if (passcode.length != 6) {
            return ERROR_LENGTH_PASSCODE
        }
        if (passcode != rePasscode) {
            return ERROR_RE_PASSCODE
        }
        return SUCCESS
    }

    private fun initListener() {
        binding.btnOk.setOnClickListener {
            when (checkPasscode()) {
                ERROR_LENGTH_PASSCODE -> {
                    context?.toast(getString(R.string.notify_length_passcode))
                }

                ERROR_RE_PASSCODE -> {
                    context?.toast(getString(R.string.notify_same_passcode))
                }

                SUCCESS -> {
                    context?.toast(getString(R.string.notify_success_passcode))
                    setupPasscode(passcode)
                    dismiss()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            setupPasscode("")
            this@SetupPasscodeDialog.dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }

    companion object {
        private const val ERROR_LENGTH_PASSCODE = 0
        private const val ERROR_RE_PASSCODE = 1
        private const val SUCCESS = 2
    }
}