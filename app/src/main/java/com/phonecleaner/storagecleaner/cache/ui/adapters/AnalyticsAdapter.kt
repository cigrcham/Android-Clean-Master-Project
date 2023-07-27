package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.base.BaseViewHolder
import com.phonecleaner.storagecleaner.cache.data.model.entity.AnalyticModel
import com.phonecleaner.storagecleaner.cache.databinding.AnalyticViewholderBinding
import com.phonecleaner.storagecleaner.cache.ui.fragment.AnalyticsFragment

class AnalyticsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    private lateinit var bindingViewHolder: AnalyticViewholderBinding
    private val listFileApps:MutableList<AnalyticModel> = mutableListOf()
    private var lastPosition = -1
    private var onClickEvent: AnalyticsFragment.OnClickAnalytic? = null

    fun setupListener(onClickEvent: AnalyticsFragment.OnClickAnalytic) {
        this.onClickEvent = onClickEvent
    }

    fun setDataList(fileAppList: List<AnalyticModel>) {
        this.listFileApps.clear()
        this.listFileApps.addAll(fileAppList)
        this.notifyDataSetChanged()
    }

    fun addDataList(value: AnalyticModel) {
        listFileApps.forEachIndexed { index: Int, fileApp: AnalyticModel ->
            if (fileApp.title == value.title) {
                listFileApps.removeAt(index = index)
                listFileApps.add(value)
                this.notifyDataSetChanged()
                return
            }
        }
        this.listFileApps.add(value)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        bindingViewHolder =
            AnalyticViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnalyticsViewHolder(binding = bindingViewHolder)
    }

    override fun getItemCount(): Int = listFileApps.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AnalyticsViewHolder).bind(listFileApps[position])
        setAnimation(holder.itemView, position)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    inner class AnalyticsViewHolder(var binding: AnalyticViewholderBinding) :
        BaseViewHolder<AnalyticModel>(binding.root) {
        override fun bind(item: AnalyticModel) {
            binding.apply {
                txtTitle.text = context.getString(item.title)
                totalFile.text = item.size.toString()
                root.setOnClickListener {
                    onClickEvent?.onClick(item = item)
                }
            }
        }
    }
}