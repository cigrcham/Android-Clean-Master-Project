package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phonecleaner.storagecleaner.cache.base.BaseDialog
import com.phonecleaner.storagecleaner.cache.databinding.DialogStoragePermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionDialog : BaseDialog() {

    lateinit var binding: DialogStoragePermissionBinding
    var callBack: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogStoragePermissionBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnOk.setOnClickListener {
                callBack?.invoke(true)
                dismiss()
            }
            btnCancel.setOnClickListener {
                callBack?.invoke(false)
                dismiss()
            }
        }
    }
}