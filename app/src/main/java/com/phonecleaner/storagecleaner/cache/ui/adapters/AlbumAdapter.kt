package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFolderNewGridBinding
import com.phonecleaner.storagecleaner.cache.extension.setSelectedFolder
import com.phonecleaner.storagecleaner.cache.utils.Constants
import java.io.File

class AlbumAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val myTag: String = this::class.java.simpleName
    private val data: ArrayList<Folder> = arrayListOf()
    private var onClickEvent: BaseFragment.OnClickFolderEvent? = null
    private var swipeView = 0
    var isSelect = false

    fun setOnClickEvent(callback: BaseFragment.OnClickFolderEvent) {
        this.onClickEvent = callback
    }

    fun setData(fileAppList: MutableList<Folder>) {
        this.data.clear()
        this.data.addAll(fileAppList)
        notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        this.swipeView = swipeView
    }

    fun multiSelect(isSelect: Boolean) {
        this.isSelect = isSelect
        data.setSelectedFolder(isSelect)
        notifyDataSetChanged()
    }

    fun onDeleteFolder(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun onRenameFolder(name: String, position: Int) {
        val itemFile = data[position]
        itemFile.name = name
        data[position] = itemFile
        notifyItemChanged(position)
    }

    inner class ListViewHolder(var binding: ItemFileLinearBinding) :
        BaseViewHolder<Folder>(binding.root) {
        override fun bind(item: Folder) {
            binding.apply {
                Glide.with(itemView.context).load(File(item.coverPath))
                    .error(R.drawable.ic_file_unknow).into(imgThumbnails)

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

    inner class GridViewHolder(var binding: ItemFolderNewGridBinding) :
        BaseViewHolder<Folder>(binding.root) {
        override fun bind(item: Folder) {
            binding.apply {
                Glide.with(itemView.context).load(File(item.coverPath))
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (swipeView == Constants.LIST_ITEM) {
            ListViewHolder(
                ItemFileLinearBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            GridViewHolder(
                ItemFolderNewGridBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ListViewHolder -> {
                holder.bind(data[position])
            }

            is GridViewHolder -> {
                holder.bind(data[position])
            }

            else -> {

            }
        }
    }

    override fun getItemCount(): Int {
        return if (data.size > 0) data.size else 0
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}