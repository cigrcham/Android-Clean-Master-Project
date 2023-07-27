package com.phonecleaner.storagecleaner.cache.base.dropBox.internal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileHelper

/**
 * Adapter for file list
 */

class FilesAdapter(
    private var loadThumbnail: ((FileMetadata, ImageView) -> Unit)? = null,
    private var itemOnClickListener: (Metadata) -> Unit = {},
    private var itemOnSelectListener: ((Metadata, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var files = mutableListOf<Metadata>()
    private var swipeView = 0
    private lateinit var listChecked: MutableList<Boolean>

    fun setFiles(files: List<Metadata>?) {
        this.files.clear()
        if (files != null) {
            this.files.addAll(files)
            listChecked = MutableList(files.size) { false }
        }
        notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        this.swipeView = swipeView
    }

    fun selectAll() {
        listChecked = MutableList(files.size) { true }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Constants.LIST_ITEM) {
            MetadataViewLinerHolder(
                ItemFileLinearBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            MetadataViewGridHolder(
                ItemFileGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        files.getOrNull(i)?.let { item ->
            when (holder) {
                is MetadataViewLinerHolder -> {
                    holder.onBind(item, i)
                }
                is MetadataViewGridHolder -> {
                    holder.onBind(item, i)
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        files.getOrNull(position)?.let {
            return it.pathLower.hashCode().toLong()
        }
        return -1L
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun getItemViewType(position: Int): Int {
        return swipeView
    }

    inner class MetadataViewLinerHolder(itemView: ItemFileLinearBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private var binding = itemView

        fun onBind(item: Metadata, position: Int) {
            binding.tvTitle.text = item.name
            binding.cbSelected.isChecked = listChecked[position]
            val mime = MimeTypeMap.getSingleton()
            val ext = item.name.substring(item.name.indexOf(".") + 1)
            val type = mime.getMimeTypeFromExtension(ext)
            if (item is FileMetadata) {
                binding.tvDetail.text = FileHelper.sizeFormat(item.size)
                binding.tvDate.text = FileHelper.dateFormat(item.serverModified.time)
                if (type != null) {
                    when {
                        type.startsWith("image/") -> {
                            loadThumbnail?.let { it(item, binding.imgThumbnails) }
                        }

                        type.startsWith("application/rar") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_rar)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/zip") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_zip)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/tar") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_tar)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/7z") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_7z)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/pdf") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_pdf)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/txt") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_txt)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/docx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_docx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/xlsx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_xlsx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/pptx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_pptx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("audio/") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_mp3)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("video/") -> {
                            binding.imgPlay.visibility = View.VISIBLE
                            loadThumbnail?.let { it(item, binding.imgThumbnails) }
                        }

                        else -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_un_known)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }
                    }
                } else {
                    Glide.with(itemView.context)
                        .load(android.R.drawable.ic_popup_sync)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imgThumbnails)
                }
            }

            if (item is FolderMetadata) {
                binding.tvDetail.visibility = View.GONE
                binding.tvDate.visibility = View.GONE
                Glide.with(itemView.context)
                    .load(R.drawable.ic_internal_folder_expanded)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.imgThumbnails)
            }

            itemView.setOnClickListener {
                itemOnClickListener(item)
            }

            binding.cbSelected.setOnClickListener {
                listChecked[position] = !listChecked[position]
                binding.cbSelected.isChecked = listChecked[position]
                itemOnSelectListener?.invoke(item, listChecked[position])
            }
        }
    }

    inner class MetadataViewGridHolder(itemView: ItemFileGridBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private var binding = itemView

        fun onBind(item: Metadata, position: Int) {
            binding.tvTitle.text = item.name
//            binding.imgSelected.setImageResource(if (listChecked[position]) R.drawable.ic_file_selected else R.drawable.ic_file_unselected)
            val mime = MimeTypeMap.getSingleton()
            val ext = item.name.substring(item.name.indexOf(".") + 1)
            val type = mime.getMimeTypeFromExtension(ext)
            if (item is FileMetadata) {
                if (type != null) {
                    when {
                        type.startsWith("image/") -> {
                            loadThumbnail?.let { it(item, binding.imgThumbnails) }
                        }

                        type.startsWith("application/rar") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_rar)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/zip") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_zip)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/tar") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_tar)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/7z") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_7z)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/pdf") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_pdf)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/txt") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_txt)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/docx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_docx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/xlsx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_xlsx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("application/pptx") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_pptx)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("audio/") -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_mp3)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }

                        type.startsWith("video/") -> {
                            loadThumbnail?.let { it(item, binding.imgThumbnails) }
                        }

                        else -> {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_un_known)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.imgThumbnails)
                        }
                    }
                } else {
                    Glide.with(itemView.context)
                        .load(android.R.drawable.ic_popup_sync)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imgThumbnails)
                }
            }

            if (item is FolderMetadata) {
                Glide.with(itemView.context)
                    .load(R.drawable.ic_internal_folder_expanded)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.imgThumbnails)
            }

            itemView.setOnClickListener {
                itemOnClickListener(item)
            }

            binding.imgSelected.setOnClickListener {
                listChecked[position] = !listChecked[position]
//                binding.imgSelected.setImageResource(if (listChecked[position]) R.drawable.ic_file_selected else R.drawable.ic_file_unselected)
                itemOnSelectListener?.invoke(item, listChecked[position])
            }
        }
    }
}