package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogDeleteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteDialog(private var getStateDelete: ((Boolean, Boolean) -> Unit)? = null) :
    DialogFragment() {
    private lateinit var binding: DialogDeleteBinding
    private var isDeletePermanently: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogDeleteBinding.inflate(layoutInflater, container, false)
        isCancelable = false
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.layoutDeletePermanently.setOnClickListener {
            isDeletePermanently = binding.checkbox.isChecked
        }
        // Delete permanently
        binding.btnOk.setOnClickListener {
            getStateDelete?.invoke(true, true)
            this@DeleteDialog.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            getStateDelete?.invoke(false, false)
            this@DeleteDialog.dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}