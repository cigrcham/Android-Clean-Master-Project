package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FilterType
import com.phonecleaner.storagecleaner.cache.data.model.entity.Recovery
import com.phonecleaner.storagecleaner.cache.databinding.ItemDateTimeBinding
import com.phonecleaner.storagecleaner.cache.databinding.ItemFileGridBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToSimpleDate
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isDocument
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isPdf
import com.phonecleaner.storagecleaner.cache.extension.isPptx
import com.phonecleaner.storagecleaner.cache.extension.isTxt
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isXlsx
import com.phonecleaner.storagecleaner.cache.extension.isZip
import com.phonecleaner.storagecleaner.cache.utils.Constants
import java.util.Calendar

class RecoveryDetailsAdapter(val canSelected: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var recoveryItem: Recovery? = null
    var listItemByDate = mutableListOf<Any>()
    var onItemSelectedListener: ((listFileApp: java.util.ArrayList<FileApp>) -> Unit)? = null
    fun setData(listFile: java.util.ArrayList<FileApp>, filterType: FilterType) {
        this.recoveryItem = recoveryItem
        listItemByDate.clear()
        listFile.sortedByDescending { it.dateModified }.filter { fileApp ->
            when (filterType) {
                FilterType.SIX_MONTHS -> {
                    val currentTime = Calendar.getInstance()
                    currentTime.add(Calendar.MONTH, -6)
                    val sixMonthsAgo = currentTime.timeInMillis
                    fileApp.dateModified > sixMonthsAgo
                }

                FilterType.ONE_YEAR -> {
                    val currentTime = Calendar.getInstance()
                    currentTime.add(Calendar.MONTH, -12)
                    val sixMonthsAgo = currentTime.timeInMillis
                    fileApp.dateModified > sixMonthsAgo
                }

                FilterType.TWO_YEARS -> {
                    val currentTime = Calendar.getInstance()
                    currentTime.add(Calendar.YEAR, -2)
                    val sixMonthsAgo = currentTime.timeInMillis
                    fileApp.dateModified > sixMonthsAgo
                }

                FilterType.ALL -> {
                    true
                }
            }
        }.groupBy { it.dateModified.convertToSimpleDate() }

            .forEach {
                listItemByDate.add(it.key)
                listItemByDate.addAll(it.value)
            }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_DATE) {
            val binding =
                ItemDateTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DateViewHolder(binding)
        } else {
            val binding =
                ItemFileGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return listItemByDate.size
    }

    override fun getItemViewType(position: Int): Int {
        if (listItemByDate.get(position) is String) {
            return TYPE_DATE
        } else {
            return TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        listItemByDate.getOrNull(position)?.let {
            when (it) {
                is String -> {
                    (holder as DateViewHolder).bindData(it)
                }

                is FileApp -> {
                    (holder as ItemViewHolder).bindData(it, canSelected) {
                        onItemSelectedListener?.invoke(ArrayList(listItemByDate.filter { it is FileApp }
                            .map { it as FileApp }.filter { it.isSelected }))
                    }
                }

                else -> {}
            }
        }
    }

    class ItemViewHolder(val binding: ItemFileGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            fileApp: FileApp,
            canSelected: Boolean = true,
            onItemSelectedListener: (fileApp: FileApp) -> Unit
        ) {
            if (fileApp.isImage()) {
                Glide.with(binding.root.context).load(fileApp.path).override(200)
                    .error(R.drawable.ic_file_unknow).into(binding.imgThumbnails)
            }

            if (fileApp.isVideo()) {
                Glide.with(binding.root.context).load(Constants.VIDEO.plus(fileApp.path))
                    .override(200).error(R.drawable.song_default).into(binding.imgThumbnails)
            }
            if (fileApp.isAudio()) {
                Glide.with(binding.root.context).load(Constants.AUDIO.plus(fileApp.path))
                    .override(200).error(R.drawable.song_default).into(binding.imgThumbnails)
            }
            if (fileApp.isZip()) {
                binding.imgThumbnails.setImageResource(R.drawable.ic_zip)
            }

            if (fileApp.isDocument()) {
                when {
                    fileApp.isTxt() -> {
                        binding.imgThumbnails.setImageResource(R.drawable.ic_txt)
                    }

                    fileApp.isPptx() -> {
                        binding.imgThumbnails.setImageResource(R.drawable.ic_pptx)
                    }

                    fileApp.isXlsx() -> {
                        binding.imgThumbnails.setImageResource(R.drawable.ic_xlsx)
                    }

                    fileApp.isPdf() -> {
                        binding.imgThumbnails.setImageResource(R.drawable.ic_pdf)
                    }

                    else -> {
                        binding.imgThumbnails.setImageResource(R.drawable.ic_file_unknow)
                    }
                }
            }
            binding.tvTitle.isVisible = !fileApp.isImage()
            binding.tvTitle.text = fileApp.name

            binding.imgSelected.isVisible = canSelected

            binding.imgSelected.setOnClickListener {
                fileApp.isSelected = !fileApp.isSelected
                binding.imgSelected.isChecked = fileApp.isSelected
                onItemSelectedListener.invoke(fileApp)
            }
        }
    }

    class DateViewHolder(val binding: ItemDateTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(date: String) {
            binding.tvDate.text = date
        }
    }

    companion object {
        const val TYPE_DATE = 1
        const val TYPE_ITEM = 2
    }
}