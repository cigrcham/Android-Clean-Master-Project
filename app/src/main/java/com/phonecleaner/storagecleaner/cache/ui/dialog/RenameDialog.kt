package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogRenameBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RenameDialog(
    private var setName: ((Boolean, String) -> Unit)? = null
) : DialogFragment() {
    private lateinit var binding: DialogRenameBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogRenameBinding.inflate(layoutInflater, container, false)
        initListener()
        isCancelable = true
        return binding.root
    }

    private fun initListener() {
        binding.apply {
            btnOk.setOnClickListener {
                if (TextUtils.isEmpty(binding.editName.text)) {
                    context?.toast(getString(R.string.notify_rename))
                } else {
                    setName?.invoke(true, binding.editName.text.toString())
                }
                this@RenameDialog.dismiss()
            }
            btnCancel.setOnClickListener {
                setName?.invoke(false, binding.editName.text.toString())
                this@RenameDialog.dismiss()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}
