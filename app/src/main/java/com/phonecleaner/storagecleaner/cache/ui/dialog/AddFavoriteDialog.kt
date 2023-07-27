package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.FragmentAddFavoriteBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToSize
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isDirectory
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isPdf
import com.phonecleaner.storagecleaner.cache.extension.isPptx
import com.phonecleaner.storagecleaner.cache.extension.isTxt
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isXlsx
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.extension.shareMultiFile
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddFavoriteDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAddFavoriteBinding
    private var isRecent: Boolean = false
    private var fileApp: FileApp? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFavoriteBinding.inflate(inflater, container, false)
        this@AddFavoriteDialog.isCancelable = true
        if (File(fileApp?.path).isDirectory) binding.btnFavorite.visibility = View.GONE
        binding.textFavorite.text =
            if (isRecent) getString(R.string.add_to_favorite) else getString(R.string.delete_from_favorite)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileApp?.let { file ->
            binding.apply {
                binding.title.text = file.name
                binding.txtSize.text = file.size.convertToSize()
                binding.txtDate.text = file.dateModified.convertToDate()
                binding.txtType.text = File(file.path).extension
                when {
                    File(file.path).isDirectory -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_folder)
                        binding.txtType.text = getString(R.string.folder)
                    }

                    file.isVideo() -> {
                        Glide.with(requireContext()).load(File(file.path))
                            .error(R.drawable.song_default).into(imagePreview)
                    }

                    file.isAudio() -> {
                        Glide.with(requireContext()).load(Constants.AUDIO.plus(file.path))
                            .error(R.drawable.song_default).into(imagePreview)
                    }

                    file.isApk() -> {
                        imagePreview.setImageResource(R.drawable.ic_apk)
                    }

                    file.isZip() -> {
                        imagePreview.setImageResource(R.drawable.ic_zip)
                    }

                    file.isTxt() -> {
                        imagePreview.setImageResource(R.drawable.ic_txt)
                    }

                    file.isPptx() -> {
                        imagePreview.setImageResource(R.drawable.ic_pptx)
                    }

                    file.isXlsx() -> {
                        imagePreview.setImageResource(R.drawable.ic_xlsx)
                    }

                    file.isPdf() -> {
                        imagePreview.setImageResource(R.drawable.ic_pdf)
                    }

                    file.isImage() -> {
                        Glide.with(requireContext()).load(File(file.path)).error(R.drawable.image)
                            .into(imagePreview)
                    }

                    else -> {
                        imagePreview.setImageResource(R.drawable.ic_file_unknow)
                    }
                }
            }
        }
        initListener()
    }

    private fun initListener() {
        binding.apply {
            containerShare.setOnClickListener(this@AddFavoriteDialog)
            containerEmail.setOnClickListener(this@AddFavoriteDialog)
            btnDelete.setOnClickListener(this@AddFavoriteDialog)
            btnFavorite.setOnClickListener(this@AddFavoriteDialog)
            btnDisplayInfo.setOnClickListener(this@AddFavoriteDialog)
        }
    }

    @SuppressLint("IntentReset")
    override fun onClick(view: View?) {
        when (view) {
            binding.btnDisplayInfo -> {
                val dialogProperties = PropertiesDialog()
                fileApp?.let {
                    dialogProperties.setFileApp(it)
                }
                dialogProperties.show(
                    parentFragmentManager, DialogProperties::class.java.simpleName
                )
            }

            binding.btnFavorite -> {
                fileApp?.let { (activity as MainActivity).favoriteFile(it) }
            }

            binding.btnDelete -> {
                val dialog =
                    DeleteDialog(getStateDelete = { isDeletePermanently: Boolean, isDelete: Boolean ->
                        if (isDelete) {
                            fileApp?.let {
                                (activity as MainActivity).deleteFile(
                                    isDeletePermanently, it
                                )
                            }
                        }
                    })
                dialog.show(parentFragmentManager, Constants.DIALOG_DELETE)
            }

            binding.containerShare -> {
                if (fileApp?.isDirectory() == true) {
                    requireContext().toast(getString(R.string.notify_share_folder))
                } else {
                    requireContext().shareMultiFile(
                        pathList = fileApp?.let { listOf(it.path) } as MutableList<String>, type = "*"
                    )
                }
            }

            binding.containerEmail -> {
                val openMail = Intent(Intent.ACTION_SEND)
                openMail.type =
                    "application/*" // Replace "application/*" with the correct MIME type of your file
                openMail.data = Uri.parse("mailto:")
                openMail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileApp?.convertToFile()))
                openMail.putExtra(
                    Intent.EXTRA_SUBJECT, "File from app ${getString(R.string.app_name)}"
                )
                startActivity(Intent.createChooser(openMail, "Send Email"))
            }
        }
    }

    companion object {
        fun onSetUpView(fileApp: FileApp, isRecent: Boolean): AddFavoriteDialog {
            val dialog = AddFavoriteDialog()
            dialog.isRecent = isRecent
            dialog.fileApp = fileApp
            return dialog
        }
    }
}
