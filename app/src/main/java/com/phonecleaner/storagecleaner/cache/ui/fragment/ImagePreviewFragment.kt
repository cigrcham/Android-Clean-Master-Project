package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.phonecleaner.storagecleaner.cache.base.BaseDialogFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.FragmentImagePreviewBinding
import com.phonecleaner.storagecleaner.cache.extension.shareFile
import com.phonecleaner.storagecleaner.cache.ui.dialog.PropertiesDialog
import com.phonecleaner.storagecleaner.cache.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ImagePreviewFragment : BaseDialogFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentImagePreviewBinding
    var fileApp: FileApp? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initData() {
        binding.toolbar.tvTitle.text = fileApp?.name
        context?.let { ct ->
            Glide.with(ct).load(File(fileApp?.path.toString()))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).transition(DrawableTransitionOptions.withCrossFade()).into(binding.imgPreview)
        }
    }

    override fun initUi() {
        binding.toolbar.imgOpenMenu.isVisible = false
        binding.toolbar.layoutMenu.isVisible = false
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener {
            this.dismiss()
        }

        binding.imgShare.setOnClickListener {
            fileApp?.path?.let { File(it) }?.let { context?.shareFile(it) }
        }

        binding.imgProperties.setOnClickListener {
            val propertiesDialog = PropertiesDialog()
            fileApp?.let { propertiesDialog.setFileApp(it) }
            propertiesDialog.show(parentFragmentManager, Constants.DIALOG_PROPERTIES)
        }
        binding.toolbar.btnBack.setOnClickListener {
            this.dismiss()
        }
    }

    companion object {
        fun onSetupView(file: FileApp): ImagePreviewFragment {
            val dialog = ImagePreviewFragment()
            dialog.fileApp = file
            return dialog
        }
    }
}