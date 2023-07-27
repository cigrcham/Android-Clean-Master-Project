package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogZipBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ZipFileDialog(
    private val title: String, private var setName: ((Boolean, String) -> Unit)? = null
) : DialogFragment() {
    private lateinit var binding: DialogZipBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogZipBinding.inflate(layoutInflater, container, false)
        isCancelable = true
        initUi()
        initListener()
        return binding.root
    }

    private fun initUi() {
        binding.tvTitle.text = title
        binding.btnOk.text = title
    }

    private fun initListener() {
        binding.btnOk.setOnClickListener {
            if (TextUtils.isEmpty(binding.editName.text)) {
                context?.toast(getString(R.string.notify_rename))
            } else setName?.invoke(true, binding.editName.text.toString())
            this@ZipFileDialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            setName?.invoke(false, binding.editName.text.toString())
            this@ZipFileDialog.dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}