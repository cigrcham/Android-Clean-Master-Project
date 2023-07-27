package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogAboutUsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutUsDialog : DialogFragment() {
    private lateinit var binding: DialogAboutUsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAboutUsBinding.inflate(layoutInflater, container, false)
        initListener()
        return binding.root
    }


    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}