package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.dropbox.core.v2.files.FileMetadata
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.databinding.DialogPropertiesBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.getExtensionFile
import com.phonecleaner.storagecleaner.cache.extension.isDirectory
import com.phonecleaner.storagecleaner.cache.utils.FileHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PropertiesDialog : DialogFragment() {
    private lateinit var binding: DialogPropertiesBinding
    private var fileApp: FileApp? = null
    private var appInstalled: AppInstalled? = null
    private var folder: Folder? = null
    private var metadata: com.dropbox.core.v2.files.Metadata? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AlertDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogPropertiesBinding.inflate(layoutInflater, container, false)
        initUi()
        isCancelable = true
        return binding.root
    }

    fun setFileApp(file: FileApp) {
        this.fileApp = file
    }

    fun setAppInstalled(appInstalled: AppInstalled) {
        this.appInstalled = appInstalled
    }

    fun setFolder(folder: Folder) {
        this.folder = folder
    }

    fun setFileDropbox(metadata: com.dropbox.core.v2.files.Metadata) {
        this.metadata = metadata
    }

    private fun initUi() {
        fileApp?.let { file ->
            binding.tvNameContent.text = file.name
            binding.tvPathContent.text = file.path
            if (file.isDirectory()) {
                if (File(file.path).listFiles() == null) {
                    binding.tvSizeContent.text = "0Kb"
                } else {
                    binding.tvSizeContent.text =
                        FileHelper.sizeFormat(File(file.path).walkTopDown().filter { it.isFile }
                            .map { it.length() }.sum())
                }
                binding.layoutFormat.isGone = true
            } else {
                binding.tvSizeContent.text = FileHelper.sizeFormat(file.size)
                binding.tvFormatContent.text = File(file.name).extension
            }
            binding.tvDateContent.text = file.dateModified.convertToDate()
        }
        appInstalled?.let { app ->
            binding.tvNameContent.text = app.appName
            binding.tvPathContent.text = app.packageName
            binding.tvSizeContent.text = FileHelper.sizeFormat(app.size)
            binding.tvDateContent.text = app.modified.convertToDate()
            binding.tvFormatContent.text = APK_EXTENSION
        }
        folder?.let { album ->
            binding.tvNameContent.text = album.name
            binding.tvPathContent.text = album.path
            binding.tvSizeContent.text =
                FileHelper.sizeFormat(File(album.path).walkTopDown().filter { it.isFile }
                    .map { it.length() }.sum())
            binding.tvDateContent.text = File(album.path).lastModified().convertToDate()
            binding.layoutFormat.isGone = true
        }
        metadata?.let { file ->
            binding.tvNameContent.text = file.name
            binding.tvPathContent.text = file.pathLower
            try {
                binding.tvSizeContent.text = FileHelper.sizeFormat((file as FileMetadata).size)
                binding.tvDateContent.text = FileHelper.dateFormat(file.serverModified.time)
                binding.tvFormatContent.text = file.name.getExtensionFile()
            } catch (e: Exception) {
                binding.layoutSize.isGone = true
                binding.tvDate.isGone = true
                binding.layoutFormat.isGone = true
            }
        }
    }

    companion object {
        private const val APK_EXTENSION = "apk"
    }
}

