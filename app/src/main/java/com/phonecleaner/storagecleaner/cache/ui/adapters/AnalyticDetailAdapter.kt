package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.ViewholderGridAnalyticBinding
import com.phonecleaner.storagecleaner.cache.databinding.ViewholderLinearAnalyticBinding
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
import com.phonecleaner.storagecleaner.cache.utils.Constants
import java.io.File

class AnalyticDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val listFileApps: MutableList<FileApp> = mutableListOf()
    private var swipeView: Int = Constants.LIST_ITEM

    fun setDataList(data: List<FileApp>) {
        this.listFileApps.clear()
        this.listFileApps.addAll(data)
        this.notifyDataSetChanged()
    }

    fun changeView(swipeView: Int) {
        this.swipeView = swipeView
    }

    fun onRenameFile(name: String, position: Int) {
        val itemFile = listFileApps.get(position)
        val file = itemFile.convertToFile()
        itemFile.name = "$name.${file.extension}"
        itemFile.path = "${file.parentFile?.absolutePath}/${itemFile.name}"
        itemFile.isSelected = false
        listFileApps.removeAt(position)
        listFileApps.add(position, itemFile)
        this.notifyItemChanged(position)
    }

    fun onDeleteFile(position: Int) {
        if (listFileApps.isEmpty()) {
            listFileApps.addAll(listOf())
            this.notifyDataSetChanged()
        } else {
            if (itemCount > position - 1) {
                listFileApps.removeAt(position)
                this.notifyItemRemoved(position)
            }
        }
    }

    fun onDeleteFile(listPosition: MutableList<Int>) {
        listPosition.forEach { position ->
            if (listFileApps.isEmpty()) {
                listFileApps.addAll(listOf())
                this.notifyDataSetChanged()
            } else {
                if (itemCount > position - 1) {
                    listFileApps.removeAt(position)
                    this.notifyItemRemoved(position)
                }
            }
        }
    }

    fun unSelectedPosition(position: Int) {
        listFileApps[position].isSelected = false
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (swipeView == Constants.GRID_ITEM) {
            GridAnalyticViewHolder(
                ViewholderGridAnalyticBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            LinearAnalyticViewHolder(
                ViewholderLinearAnalyticBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int = listFileApps.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (swipeView == Constants.LIST_ITEM) (holder as LinearAnalyticViewHolder).bind(
            listFileApps[position]
        )
        else (holder as GridAnalyticViewHolder).bind(listFileApps[position])
    }

    private var onClickEvent: BaseFragment.OnClickEvent? = null
    fun setOnClickEvent(callback: BaseFragment.OnClickEvent) {
        this.onClickEvent = callback
    }

    inner class LinearAnalyticViewHolder(val binding: ViewholderLinearAnalyticBinding) :
        BaseViewHolder<FileApp>(binding.root) {
        override fun bind(item: FileApp) {
            binding.apply {
                cbSelected.setOnClickListener {
                    item.isSelected = !item.isSelected
                    cbSelected.isChecked = item.isSelected
                    onClickEvent?.onSelectItem(item, position = position)
                }
                tvTitle.text = item.name
                tvSize.text = item.size.convertToSize()
                tvModifier.text = item.dateModified.convertToDate()
                root.setOnClickListener {
                    onClickEvent?.onClickItem(item, position = position)
                }
                val file = File(item.path)
                when {
                    item.iconBitmap != null -> {
                        Glide.with(itemView.context).load(item.iconBitmap).error(R.drawable.apps)
                            .optionalCenterCrop().transition(
                                DrawableTransitionOptions.withCrossFade()
                            ).into(imagePreview)
                    }

                    File(item.path).isDirectory -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_folder)
                        binding.tvDetail.text =
                            binding.root.context.getString(R.string.item_count, file.getFileCount())
                    }

                    item.isVideo() -> {
                        Glide.with(itemView.context).load(File(item.path))
                            .error(R.drawable.song_default).into(binding.imagePreview)
                    }

                    item.isAudio() -> {
                        Glide.with(itemView.context).load(Constants.AUDIO.plus(item.path))
                            .error(R.drawable.song_default).into(binding.imagePreview)
                    }

                    item.isApk() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_apk)
                    }

                    item.isZip() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_zip)
                    }

                    item.isTxt() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_txt)
                    }

                    item.isPptx() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_pptx)
                    }

                    item.isXlsx() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_xlsx)
                    }

                    item.isPdf() -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_pdf)
                    }

                    item.isImage() -> {
                        Glide.with(itemView.context).load(File(item.path)).error(R.drawable.image)
                            .into(binding.imagePreview)
                    }

                    else -> {
                        binding.imagePreview.setImageResource(R.drawable.ic_file_unknow)
                    }
                }

            }
        }
    }

    inner class GridAnalyticViewHolder(val binding: ViewholderGridAnalyticBinding) :
        BaseViewHolder<FileApp>(binding.root) {
        override fun bind(item: FileApp) {
            binding.apply {
                binding.apply {
                    cbSelected.setOnClickListener {
                        item.isSelected = !item.isSelected
                        cbSelected.isChecked = item.isSelected
                        onClickEvent?.onSelectItem(item, position = position)
                    }
                    tvTitle.text = item.name
                    root.setOnClickListener {
                        onClickEvent?.onClickItem(item, position = position)
                    }

                    val file = File(item.path)
                    when {
                        item.iconBitmap != null -> {
                            Glide.with(itemView.context).load(item.iconBitmap)
                                .error(R.drawable.apps).optionalCenterCrop().transition(
                                    DrawableTransitionOptions.withCrossFade()
                                ).into(imagePreview)
                        }

                        File(item.path).isDirectory -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_folder)
                            binding.tvTitle.text = binding.root.context.getString(
                                R.string.item_count, file.getFileCount()
                            )
                        }

                        item.isVideo() -> {
                            Glide.with(itemView.context).load(File(item.path))
                                .error(R.drawable.song_default).into(binding.imagePreview)
                        }

                        item.isAudio() -> {
                            Glide.with(itemView.context).load(Constants.AUDIO.plus(item.path))
                                .error(R.drawable.song_default).into(binding.imagePreview)
                        }

                        item.isApk() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_apk)
                        }

                        item.isZip() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_zip)
                        }

                        item.isTxt() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_txt)
                        }

                        item.isPptx() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_pptx)
                        }

                        item.isXlsx() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_xlsx)
                        }

                        item.isPdf() -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_pdf)
                        }

                        item.isImage() -> {
                            Glide.with(itemView.context).load(File(item.path))
                                .error(R.drawable.image).into(binding.imagePreview)
                        }

                        else -> {
                            binding.imagePreview.setImageResource(R.drawable.ic_file_unknow)
                        }
                    }

                }
            }
        }
    }
}