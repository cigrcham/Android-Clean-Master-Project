package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToSize
import com.phonecleaner.storagecleaner.cache.extension.getFileCount
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isPdf
import com.phonecleaner.storagecleaner.cache.extension.isPptx
import com.phonecleaner.storagecleaner.cache.extension.isTxt
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isXlsx
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.extension.setSelectedFile
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileHelper
import java.io.File

class FilesAdapter(var from: String? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val myTag: String = this::class.java.simpleName
    private val fileAppList: MutableList<FileApp> = mutableListOf()
    private var swipeView = 0
    var isSelect = false
    private var onClickEvent: BaseFragment.OnClickEvent? = null

    fun setOnClickEvent(callback: BaseFragment.OnClickEvent) {
        this.onClickEvent = callback
    }

    fun setData(fileAppList: List<FileApp>) {
        this.fileAppList.clear()
        this.fileAppList.addAll(fileAppList)
        notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        this.swipeView = swipeView
    }

    fun multiSelect(isSelect: Boolean) {
        this.isSelect = isSelect
        fileAppList.setSelectedFile(isSelect)
        notifyDataSetChanged()
    }

    fun removeData(list: MutableList<FileApp>) {
        this.fileAppList.clear()
        this.fileAppList.addAll(list)
        notifyDataSetChanged()
    }

    fun onRenameFile(name: String, position: Int) {
        val itemFile = fileAppList[position]
        val file = itemFile.convertToFile()
        itemFile.name = "$name.${file.extension}"
        itemFile.path = "${file.parentFile?.absolutePath}/${itemFile.name}"
        itemFile.isSelected = false
        fileAppList.removeAt(position)
        fileAppList.add(position, itemFile)
        notifyItemChanged(position)
    }

    fun onDeleteFile(listPosition: MutableList<Int>) {
        listPosition.forEach { position ->
            if (fileAppList.isEmpty()) {
                fileAppList.addAll(listOf())
                notifyDataSetChanged()
            } else {
                if (itemCount > position - 1) {
                    fileAppList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

    fun onDeleteFiles(listFile: MutableList<FileApp>) {
        listFile.forEach { file ->
            if (fileAppList.isEmpty()) {
                fileAppList.addAll(listOf())
                notifyDataSetChanged()
            } else {
                val position = fileAppList.indexOf(file)
                fileAppList.remove(file)
                notifyItemRemoved(position)
            }
        }
    }

    fun onInsertFile(list: List<FileApp>) {
        val sizeInsert = list.size
        list.forEach {
            it.isSelected = false
        }
        fileAppList.addAll(0, list)
        notifyItemRangeInserted(0, sizeInsert)
    }

    fun unSelectedPosition(position: Int) {
        fileAppList[position].isSelected = false
        notifyItemChanged(position)
    }

    inner class ListViewHolder(var binding: ItemFileLinearBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: FileApp, position: Int) {
            val file = File(item.path)
            binding.tvDetail.text = FileHelper.sizeFormat(item.size)
            binding.imgPlay.isVisible = item.isVideo()
            binding.tvTitle.text = item.name
            binding.tvDate.text = item.dateModified.convertToDate()

            binding.cbSelected.isChecked = item.isSelected

            binding.root.setOnClickListener {
                onClickEvent?.onClickItem(item, position)
            }

            binding.cbSelected.setOnClickListener {
                onClickEvent?.onItemTypeAdapter(this@FilesAdapter)
                item.isSelected = !item.isSelected
                binding.cbSelected.isChecked = item.isSelected
                onClickEvent?.onSelectItem(item, position)
            }

            when {
                file.isDirectory -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_folder)
                    binding.tvDetail.text =
                        binding.root.context.getString(R.string.item_count, file.getFileCount())
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

                else -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                }
            }
        }
    }

    inner class GridViewHolder(var binding: ItemFileGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: FileApp) {
            val file = File(item.path)
            binding.tvTitle.text = item.name
            binding.tvModifier.text = item.dateModified.convertToDate()
            binding.tvDetail.text = item.size.convertToSize()
            binding.root.setOnClickListener {
                onClickEvent?.onClickItem(item, layoutPosition)
            }

            binding.imgSelected.setOnClickListener {
                onClickEvent?.onItemTypeAdapter(this@FilesAdapter)
                item.isSelected = !item.isSelected
                onClickEvent?.onSelectItem(item, layoutPosition)
            }

            when {
                file.isDirectory -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_folder)
                    binding.tvDetail.text =
                        binding.root.context.getString(R.string.item_count, file.getFileCount())
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

                else -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (swipeView == Constants.LIST_ITEM) ListViewHolder(
            ItemFileLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        else GridViewHolder(
            ItemFileGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ListViewHolder -> {
                fileAppList.getOrNull(position)?.let { file ->
                    holder.onBind(file, position)
                }
            }

            is GridViewHolder -> {
                fileAppList.getOrNull(position)?.let { file ->
                    holder.onBind(file)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return fileAppList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}