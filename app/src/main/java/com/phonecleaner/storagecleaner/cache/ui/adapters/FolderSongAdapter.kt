package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.extension.setSelectedFolder
import com.phonecleaner.storagecleaner.cache.utils.Constants

class FolderSongAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = arrayListOf<Folder>()
    private var onClickEvent: BaseFragment.OnClickFolderEvent? = null
    private var swipeView = 0
    var isSelect: Boolean = false

    fun setOnClickEvent(callback: BaseFragment.OnClickFolderEvent) {
        this.onClickEvent = callback
    }

    fun setData(fileAppList: MutableList<Folder>) {
        this.data.clear()
        this.data.addAll(fileAppList)
        this.notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        this.swipeView = swipeView
    }

    fun multiSelect(isSelect: Boolean) {
        this.isSelect = isSelect
        this.data.setSelectedFolder(isSelect)
    }

    fun onDeleteFolder(position: Int) {
        data.removeAt(position)
        this.notifyItemRemoved(position)
    }

    fun onRenameFolder(name: String, position: Int) {
        val itemFile: Folder = data[position]
        itemFile.name = name
        data[position] = itemFile
        this.notifyItemChanged(position)
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
//                viewType > 0 && viewType % 10 == 0 -> NativeViewHolder(
//                    ItemNativeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                )

                else -> {
                    GridViewHolder(
                        ItemFileGridBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
                }
            }
        }
    }

    inner class ListViewHolder(var binding: ItemFileLinearBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class GridViewHolder(var binding: ItemFileGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
//            is BannerViewHolder -> holder.bind(BannerModel())
//            is NativeViewHolder -> holder.bind(NativeModel())
            is ListViewHolder -> {
                data.getOrNull(position)?.let { item ->
                    holder.binding.apply {
                        Glide.with(root.context).load(Constants.AUDIO.plus(item.coverPath))
                            .error(R.drawable.song_default).into(imgThumbnails)

                        tvTitle.text = item.name
                        tvDetail.text = item.listFile.size.toString()

                        root.setOnClickListener {
                            onClickEvent?.onClickItem(item, position)
                        }

                        cbSelected.isChecked = item.selected
                        cbSelected.setOnClickListener {
                            item.selected = !item.selected
                            cbSelected.isChecked = item.selected
                            onClickEvent?.onSelectItem(item, position)
                        }
                    }
                }
            }

            is GridViewHolder -> {
                data.getOrNull(position)?.let { item ->
                    holder.binding.apply {
                        Glide.with(holder.itemView.context)
                            .load(Constants.AUDIO.plus(item.coverPath))
                            .error(R.drawable.ic_file_unknow).into(imgThumbnails)

                        tvTitle.text = item.name
                        tvDetail.text = item.listFile.size.toString()

                        root.setOnClickListener {
                            onClickEvent?.onClickItem(item, position)
                        }
                        imgSelected.setOnClickListener {
                            item.selected = !item.selected
                            onClickEvent?.onSelectItem(item, position)
                        }

                    }
                }
            }

            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return if (data.size > 0) data.size else 0
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}