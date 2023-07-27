package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FilterType
import com.phonecleaner.storagecleaner.cache.databinding.ItemFilterBinding

class FilterAdapter(val onSelectedItem: (type: FilterType) -> Unit) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    private var currentFiler: FilterType? = null

    fun setFilterType(filterType: FilterType) {
        this.currentFiler = filterType
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        FilterType.values().getOrNull(position)?.let { type ->
            if (currentFiler == type) {
                holder.binding.root.setBackgroundResource(R.drawable.background_btn_filter_selected)
                holder.binding.imgDate.setImageResource(R.drawable.ic_date_white)
                holder.binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        holder.binding.root.context,
                        R.color.white
                    )
                )
            } else {
                holder.binding.root.setBackgroundResource(R.drawable.background_btn_filter_normal)
                holder.binding.imgDate.setImageResource(R.drawable.ic_date)
                holder.binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        holder.binding.root.context,
                        R.color.black
                    )
                )
            }

            when (type) {
                FilterType.SIX_MONTHS -> {
                    holder.binding.tvDate.text =
                        holder.binding.root.context.getString(R.string.six_months)
                }
                FilterType.ONE_YEAR -> {
                    holder.binding.tvDate.text =
                        holder.binding.root.context.getString(R.string.one_year)
                }
                FilterType.TWO_YEARS -> {
                    holder.binding.tvDate.text =
                        holder.binding.root.context.getString(R.string.two_years)
                }
                FilterType.ALL -> {
                    holder.binding.tvDate.text = holder.binding.root.context.getString(R.string.all)
                }
            }

            holder.binding.root.setOnClickListener {
                onSelectedItem(type)
            }
        }
    }
}