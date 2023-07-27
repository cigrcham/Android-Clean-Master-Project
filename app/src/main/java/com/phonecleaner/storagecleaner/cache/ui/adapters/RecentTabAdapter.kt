package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.ViewholderRecentTabBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.convertToSize
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isDirectory
import com.phonecleaner.storagecleaner.cache.extension.isDocx
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isPdf
import com.phonecleaner.storagecleaner.cache.extension.isPptx
import com.phonecleaner.storagecleaner.cache.extension.isTxt
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isXlsx
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.utils.Constants
import java.io.File

class RecentTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onItemEvent: BaseFragment.OnClickEvent? = null
    private var fileAppList: MutableList<FileApp> = mutableListOf()
    fun setOnClickEvent(callback: BaseFragment.OnClickEvent) {
        this.onItemEvent = callback
    }

    fun setFileAppLists(value: List<FileApp>) {
        this.fileAppList.clear()
        this.fileAppList.addAll(value)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        RecentTabViewHolder(
            binding = ViewholderRecentTabBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RecentTabViewHolder).bind(item = fileAppList[position])
    }

    override fun getItemCount(): Int = fileAppList.size

    inner class RecentTabViewHolder(private val binding: ViewholderRecentTabBinding) :
        BaseViewHolder<FileApp>(binding.root) {
        override fun bind(item: FileApp) {
            binding.apply {
                tvTitle.text = item.name
                tvSize.text = item.size.convertToSize()
                tvModifier.text = item.dateModified.convertToDate()
                root.setOnClickListener {
                    onItemEvent?.onClickItem(file = item, position = position)
                }
            }
            when {
                item.isDirectory() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_folder)
                }

                item.isVideo() -> {
                    Glide.with(itemView.context).load(File(item.path))
                        .error(R.drawable.song_default).into(binding.imgThumbnails)
                }

                item.isAudio() -> {
                    Glide.with(itemView.context).load(Constants.AUDIO.plus(item.path))
                        .error(R.drawable.song_default).into(binding.imgThumbnails)
                }

                item.isApk() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_apk)
                }

                item.isZip() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_zip)
                }

                item.isTxt() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_txt)
                }

                item.isPptx() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_pptx)
                }

                item.isXlsx() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_xlsx)
                }

                item.isPdf() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_pdf)
                }

                item.isImage() -> {
                    Glide.with(itemView.context).load(File(item.path)).error(R.drawable.image)
                        .into(binding.imgThumbnails)
                }

                item.isDocx() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.docx)
                }

                else -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                }
            }
        }
    }
}