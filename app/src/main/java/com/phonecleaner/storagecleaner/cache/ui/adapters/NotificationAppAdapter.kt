package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import com.phonecleaner.storagecleaner.cache.databinding.ItemNotificationAppBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.toTimeAgo

class NotificationAppAdapter(val onItemClick: (MessageNotifi) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val listApp: ArrayList<MessageNotifi> = arrayListOf()
    fun setData(list: List<MessageNotifi>) {
        listApp.clear()
        listApp.addAll(list)
        this@NotificationAppAdapter.notifyDataSetChanged()
    }

    fun selectAllItem(isSelectAll: Boolean) {
        listApp.map {
            it.isSelected = isSelectAll
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            ItemNotificationAppBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.onBind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return listApp.size
    }

    inner class ItemViewHolder(view: ItemNotificationAppBinding) :
        RecyclerView.ViewHolder(view.root) {
        val binding = view
        fun onBind(position: Int) {
            listApp.getOrNull(position)?.let {
                Glide.with(binding.root.context).load(Constants.APK.plus(it.packageName))
                    .error(R.drawable.ic_file_unknow).into(binding.imgPreview)

                binding.tvTitle.text = it.appName
                binding.tvContent.text = it.content
                binding.tvTimeCount.text = it.modified.toTimeAgo()

                binding.cbSelected.isChecked = it.isSelected

                binding.cbSelected.setOnCheckedChangeListener { _, isCheck ->
                    it.isSelected = isCheck
                    if (it.isSelected) {
                        binding.root.alpha = 1f
                    } else {
                        binding.root.alpha = 0.8f
                    }
                    onItemClick.invoke(it)
                }

                if (it.isSelected) {
                    binding.root.alpha = 1f
                } else {
                    binding.root.alpha = 0.8f
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}