package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseDialogFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.FragmentTextBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader

@AndroidEntryPoint
class TextFragment : BaseDialogFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var fileApp: FileApp
    private lateinit var binding: FragmentTextBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTextBinding.inflate(layoutInflater, container, false)
        initUi()
        return binding.root
    }

    override fun initUi() {
        binding.toolbar.tvTitle.text = fileApp?.name ?: ""
//        binding.toolbar.imgSelectAll.isGone = true
//        binding.toolbar.imgOpenMenu.isGone = true
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun doWork() {
        if (fileApp.convertToFile().isFile) {
            val bufferedReader: BufferedReader = fileApp.convertToFile().bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            binding.tvText.text = inputString
            Toast.makeText(requireContext(), "$inputString", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvText.text = getString(R.string.cannot_read_file)
        }
    }

//    override fun baseBackPressed() {
//        activity?.onBackPressedDispatcher?.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    onBackPressed()
//                }
//            })
//    }

    private fun onBackPressed() {
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
            (act as MainActivity).removeFragment(this)
        }
    }

    companion object {
        fun onSetupView(fileApp: FileApp): TextFragment {
            val dialog = TextFragment()
            dialog.fileApp = fileApp
            return dialog
        }
    }
}