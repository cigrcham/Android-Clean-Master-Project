package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridNewBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.setSelectedFile
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileHelper
import timber.log.Timber
import java.io.File

class ApksAdapter(var from: String? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val fileAppList = mutableListOf<FileApp>()
    private var swipeView = 0
    var isSelect = false
    var isLoadBanner = true
    private var onClickEvent: BaseFragment.OnClickEvent? = null

    fun setOnClickEvent(callback: BaseFragment.OnClickEvent) {
        this.onClickEvent = callback
    }

    fun setData(fileAppList: List<FileApp>) {
        this.fileAppList.clear()
        this.fileAppList.addAll(fileAppList)
        notifyDataSetChanged()
    }

    fun changeView(swipeView: Int, isLoad: Boolean = true) {
        isLoadBanner = isLoad
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
        fileAppList[position] = itemFile
        notifyItemChanged(position + 1 + position / 9)
    }

    fun onDeleteFile(listPosition: MutableList<Int>) {
        Timber.tag("TAG666").d("onDeleteFile: %s", listPosition)
        listPosition.forEach { position ->
            if (fileAppList.isEmpty()) {
                fileAppList.addAll(listOf())
                notifyDataSetChanged()
            } else {
                fileAppList.removeAt(position)
                notifyItemRemoved(position + 1 + position / 9)
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
        notifyItemChanged(position + 1 + position / 9)
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
                item.isSelected = !item.isSelected
                binding.cbSelected.isChecked = item.isSelected
                onClickEvent?.onSelectItem(item, position)
            }

            when {
                item.isApk() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_apk)
                }

                else -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                }
            }
        }
    }

    inner class GridViewHolder(var binding: ItemFileGridNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: FileApp, position: Int) {
            binding.tvTitle.text = item.name
            binding.root.setOnClickListener {
                onClickEvent?.onClickItem(item, layoutPosition)
            }

            binding.imgSelected.setOnClickListener {
                item.isSelected = !item.isSelected
                onClickEvent?.onSelectItem(item, layoutPosition)
            }

            when {
                item.isApk() -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_apk)
                }

                else -> {
                    binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (swipeView == Constants.LIST_ITEM) {
            when {
//                viewType == 0 -> BannerViewHolder(
//                    ItemBannerBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    )
//                )
//
//                viewType > 0 && viewType % 9 == 0 -> NativeViewHolder(
//                    ItemNativeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                )

                else -> {
                    ListViewHolder(
                        ItemFileLinearBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
                }
            }
        } else {
            when {
//                viewType == 0 -> BannerViewHolder(
//                    ItemBannerBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    )
//                )
//
//                viewType > 0 && viewType % 9 == 0 -> NativeViewHolder(
//                    ItemNativeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                )

                else -> {
                    GridViewHolder(
                        ItemFileGridNewBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
                }
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
//            is BannerViewHolder -> {
//                if (isNotShowBanner) {
//                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
//                    holder.itemView.visibility = View.GONE
//                } else {
//                    if (isLoadBanner) {
//                        Timber.d("banner duoc khoi tao $position")
//                        isLoadBanner = false
//                        holder.bind(BannerModel())
//                    }
//                }
//            }
//            is NativeViewHolder -> {
//                holder.bind(NativeModel())
//            }
            is ListViewHolder -> {
                fileAppList.getOrNull(position)?.let { file ->
                    holder.onBind(file, position)
                }
            }

            is GridViewHolder -> {
                fileAppList.getOrNull(position)?.let { file ->
                    holder.onBind(file, position)
                }
            }

            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return fileAppList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}