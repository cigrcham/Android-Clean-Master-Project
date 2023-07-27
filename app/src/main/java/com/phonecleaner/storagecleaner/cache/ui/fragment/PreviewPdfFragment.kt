package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phonecleaner.storagecleaner.cache.databinding.FragmentPdfPreviewerBinding
import com.phonecleaner.storagecleaner.cache.base.BaseDialogFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PreviewPdfFragment : BaseDialogFragment() {
    private lateinit var binding: FragmentPdfPreviewerBinding
    private var fileApp: FileApp? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfPreviewerBinding.inflate(inflater, container, false)
        binding.topAppBar.tvTitle.text = fileApp?.name ?: ""
        fileApp?.convertToFile()?.let {
            binding.pdfView.fromFile(File(it.path)).enableSwipe(true).swipeHorizontal(false)
                .enableDoubletap(true).defaultPage(0).enableAnnotationRendering(false)
                .password(null).scrollHandle(null).enableAntialiasing(true).load()
        }
        return binding.root
    }

    override fun initUi() {
    }

    override fun initListener() {
        binding.topAppBar.btnBack.setOnClickListener {
            this@PreviewPdfFragment.dismiss()
        }
    }

    companion object {
        fun onSetupView(fileApp: FileApp): PreviewPdfFragment {
            val dialog = PreviewPdfFragment()
            dialog.fileApp = fileApp
            return dialog
        }
    }
}