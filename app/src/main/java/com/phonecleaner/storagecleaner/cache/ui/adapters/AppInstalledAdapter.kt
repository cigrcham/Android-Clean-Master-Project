package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridNewBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileLinearBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToDate
import com.phonecleaner.storagecleaner.cache.extension.convertToSize
import com.phonecleaner.storagecleaner.cache.extension.setSelectedApp
import com.phonecleaner.storagecleaner.cache.utils.Constants

class AppInstalledAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data: ArrayList<AppInstalled> = arrayListOf<AppInstalled>()
    private var onClickEvent: BaseFragment.OnClickAppEvent? = null
    private var swipeView = 0
    var isSelect = false
    var isLoadBanner = true

    fun setOnClickEvent(callback: BaseFragment.OnClickAppEvent) {
        this.onClickEvent = callback
    }

    fun setData(data: MutableList<AppInstalled>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        isLoadBanner = true
        this.swipeView = swipeView
    }

    fun multiSelect(isSelect: Boolean) {
        this.isSelect = isSelect
        data.setSelectedApp(isSelect)
        notifyDataSetChanged()
    }

    fun onDeleteApp(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ListViewHolder(var binding: ItemFileLinearBinding) :
        BaseViewHolder<AppInstalled>(binding.root) {
        override fun bind(item: AppInstalled) {
            val positionList = position
            data.getOrNull(positionList)?.let { item ->
                binding.apply {
                    Glide.with(itemView.context).load(item.iconBitmap)
                        .error(R.drawable.ic_file_unknow).into(imgThumbnails)
                    tvTitle.text = item.appName
                    tvDetail.text = item.size.convertToSize()
                    tvDate.text = item.modified.convertToDate()

                    cbSelected.isChecked = item.selected

                    root.setOnClickListener {
                        onClickEvent?.onClickItem(item, positionList)
                    }

                    cbSelected.setOnClickListener {
                        item.selected = !item.selected
                        cbSelected.isChecked = item.selected
                        onClickEvent?.onSelectItem(item, positionList)
                    }
                }
            }
        }
    }

    inner class GridViewHolder(var binding: ItemFileGridNewBinding) :
        BaseViewHolder<AppInstalled>(binding.root) {
        override fun bind(item: AppInstalled) {
            val positionList = position
            data.getOrNull(positionList)?.let { item ->
                binding.apply {
                    Glide.with(itemView.context).load(item.iconBitmap)
                        .error(R.drawable.ic_file_unknow).into(imgThumbnails)
                    tvTitle.text = item.appName
                    tvDetail.text = item.size.convertToSize()
                    tvDetail.text = item.modified.convertToDate()

                    imgSelected.isChecked = item.selected

                    root.setOnClickListener {
                        onClickEvent?.onClickItem(item, positionList)
                    }

                    imgSelected.setOnClickListener {
                        item.selected = !item.selected
                        imgSelected.isChecked = item.selected
                        onClickEvent?.onSelectItem(item, positionList)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (swipeView == Constants.LIST_ITEM) {
            ListViewHolder(
                ItemFileLinearBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            GridViewHolder(
                ItemFileGridNewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ListViewHolder -> {
                holder.bind(data[position])
            }

            is GridViewHolder -> {
                holder.bind(data[position])
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