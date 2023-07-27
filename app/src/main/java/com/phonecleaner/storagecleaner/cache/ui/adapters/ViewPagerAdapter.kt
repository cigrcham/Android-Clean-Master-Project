package com.phonecleaner.storagecleaner.cache.ui.adapters

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.phonecleaner.storagecleaner.cache.base.BaseFragment

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private var listFragment = listOf<BaseFragment>()

    fun setListFragment(fragments: List<BaseFragment>) {
        this.listFragment = fragments
    }

    override fun getItemCount(): Int {
        return listFragment.size
    }

    override fun createFragment(position: Int): BaseFragment {
        return listFragment[position]
    }
}