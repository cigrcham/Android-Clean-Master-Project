package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.RecentViewHolderBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
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
import com.phonecleaner.storagecleaner.cache.utils.Constants
import java.io.File

class RecentUploadRecycleView : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var bindingViewHolder: RecentViewHolderBinding
    private val listRecent: ArrayList<FileApp> = arrayListOf()
    fun setListData(data: List<FileApp>) {
        listRecent.clear()
        listRecent.addAll(data)
        this.notifyDataSetChanged()
    }

    private var onClickEvent: BaseFragment.OnClickEvent? = null
    fun setOnClickEvent(callback: BaseFragment.OnClickEvent) {
        this.onClickEvent = callback
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RecentViewHolder).bind(listRecent[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        bindingViewHolder =
            RecentViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(bindingViewHolder)
    }

    override fun getItemCount(): Int = listRecent.size


    inner class RecentViewHolder(val binding: RecentViewHolderBinding) :
        BaseViewHolder<FileApp>(binding.root) {
        override fun bind(item: FileApp) {
            binding.titleRecent.text = item.name
            binding.type.text = item.type
            binding.size.text = item.size.convertToSize()
            binding.date.text = item.dateModified.convertToDate()
            binding.root.setOnClickListener {
                onClickEvent?.onClickItem(file = item, position = position)
            }
            when {
                item.isDirectory() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_folder)
                }

                item.isVideo() -> {
                    Glide.with(itemView.context).load(File(item.path)).error(R.drawable.video)
                        .into(binding.imageRecent)
                }

                item.isAudio() -> {
                    Glide.with(itemView.context).load(Constants.AUDIO.plus(item.path))
                        .error(R.drawable.song_default).into(binding.imageRecent)
                }

                item.isApk() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_apk)
                }

                item.isZip() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_zip)
                }

                item.isTxt() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_txt)
                }

                item.isPptx() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_pptx)
                }

                item.isXlsx() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_xlsx)
                }

                item.isPdf() -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_pdf)
                }

                item.isImage() -> {
                    Glide.with(itemView.context).load(File(item.path))
                        .error(R.drawable.ic_file_unknow).into(binding.imageRecent)
                }

                else -> {
                    binding.imageRecent.setImageResource(R.drawable.ic_folder)
                }
            }
        }
    }
}