package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.databinding.ItemAppBlockBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants

class AppBlockAdapter(val onItemClick: (AppInstalled, Boolean) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val listApp = arrayListOf<AppInstalled>()
    val listAppBlock = arrayListOf<AppInstalled>()

    fun setData(list: ArrayList<AppInstalled>) {
        listApp.clear()
        listApp.addAll(list)
        notifyDataSetChanged()
    }

    fun selectAll(isSelectAll: Boolean) {
        if (isSelectAll) {
            listAppBlock.clear()
            listAppBlock.addAll(listApp)
        } else {
            listAppBlock.clear()
        }
        notifyDataSetChanged()
    }

    fun setListAppBlock(list: List<AppInstalled>) {
        listAppBlock.clear()
        listAppBlock.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            ItemAppBlockBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
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

    inner class ItemViewHolder(view: ItemAppBlockBinding) : RecyclerView.ViewHolder(view.root) {
        val binding = view
        fun onBind(position: Int) {
            listApp.getOrNull(position)?.let {
                Glide.with(binding.root.context)
                    .load(Constants.APK.plus(it.packageName))
                    .error(R.drawable.ic_file_unknow)
                    .into(binding.imgPreview)

                binding.tvTitle.text = it.appName
                val selected = listAppBlock.find { data ->
                    data.packageName == it.packageName
                }
                binding.btnOnOff.isChecked = selected != null
                if (selected != null) {
                    binding.root.alpha = 1f
                } else {
                    binding.root.alpha = 0.8f
                }
            }

            binding.btnOnOff.setOnCheckedChangeListener { _, isCheck ->
                listApp.getOrNull(position)?.let {
                    onItemClick.invoke(it, isCheck)
                    if (isCheck) {
                        binding.root.alpha = 1f
                    } else {
                        binding.root.alpha = 0.8f
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}