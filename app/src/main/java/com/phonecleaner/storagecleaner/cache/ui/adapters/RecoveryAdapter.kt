package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.data.model.entity.RecoveryType
import com.phonecleaner.storagecleaner.cache.databinding.ItemRecoveryTypeBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants

class RecoveryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val recoveryList = ArrayList<Recovery>()
    var onItemCLick: ((Recovery) -> Unit)? = null

    fun setData(data: ArrayList<Recovery>) {
        this.recoveryList.clear()
        this.recoveryList.addAll(data)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            binding = ItemRecoveryTypeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun getItemCount(): Int = recoveryList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        recoveryList.getOrNull(position)?.let {
            (holder as ViewHolder).bind(item = it)
        }
    }

    inner class ViewHolder(private val binding: ItemRecoveryTypeBinding) : BaseViewHolder<Recovery>(
        binding.root
    ) {
        override fun bind(item: Recovery) {
            binding.apply {
                root.setOnClickListener {
                    onItemCLick?.invoke(item)
                }
                tvTitle.text = item.name
                tvCount.text = item.fileCount.toString()
                when (item.type) {
                    is RecoveryType.Image -> {
                        Glide.with(itemView.context).load(item.type.imagePath).into(imageBig)
                        imageSmall.isVisible = false
                        imageBig.isVisible = true
                    }

                    is RecoveryType.Video -> {
                        Glide.with(itemView.context).load(Constants.VIDEO.plus(item.type.videoPath))
                            .into(imageBig)
                        imageSmall.isVisible = false
                        imageBig.isVisible = true
                    }

                    is RecoveryType.Audio -> {
                        imageSmall.isVisible = true
                        imageBig.isVisible = false
                        imageSmall.setImageResource(item.type.imageRes)
                        layoutImage.setBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context, item.type.colorRes
                            )
                        )
                    }

                    is RecoveryType.Zip -> {
                        imageSmall.isVisible = true
                        imageBig.isVisible = false
                        imageSmall.setImageResource(item.type.imageRes)
                        layoutImage.setBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context, item.type.colorRes
                            )
                        )
                    }

                    is RecoveryType.Document -> {
                        imageSmall.isVisible = true
                        imageBig.isVisible = false
                        imageSmall.setImageResource(item.type.imageRes)
                        layoutImage.setBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context, item.type.colorRes
                            )
                        )
                    }
                }
            }
        }
    }
}