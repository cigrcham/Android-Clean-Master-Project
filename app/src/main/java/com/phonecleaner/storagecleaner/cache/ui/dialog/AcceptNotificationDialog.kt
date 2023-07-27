package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phonecleaner.storagecleaner.cache.base.BaseDialog
import com.phonecleaner.storagecleaner.cache.databinding.DialogNotificationPermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptNotificationDialog : BaseDialog() {
    private lateinit var binding: DialogNotificationPermissionBinding
    var callBack: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogNotificationPermissionBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnOk.setOnClickListener {
                callBack?.invoke(true)
                this@AcceptNotificationDialog.dismiss()
            }
            btnCancel.setOnClickListener {
                callBack?.invoke(false)
                this@AcceptNotificationDialog.dismiss()
            }
        }
    }
}