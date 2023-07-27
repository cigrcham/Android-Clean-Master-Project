package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.ItemHeaderExplorerBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import timber.log.Timber

class HeaderExplorerAdapter : RecyclerView.Adapter<HeaderExplorerAdapter.ViewHolder>() {

    private var listFileApp = mutableListOf<FileApp>()
    var onClickEvent: ((FileApp, Int) -> Unit)? = null

    fun setData(data: ArrayList<FileApp>) {
        Timber.d("load data Header do set $data")
        this.listFileApp.clear()
        this.listFileApp.add(FileApp(name = Constants.HOME))
        this.listFileApp.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemHeaderExplorerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHeaderExplorerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listFileApp.getOrNull(position)?.let { fileApp ->
            holder.binding.tvFolderName.text = fileApp.name
            holder.binding.root.setOnClickListener {
                onClickEvent?.invoke(fileApp, position)
            }
            holder.binding.imgNext.isGone = position == listFileApp.size - 1
        }
    }

    override fun getItemCount(): Int {
        return listFileApp.size
    }
}