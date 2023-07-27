package com.phonecleaner.storagecleaner.cache.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BasePagerAdapter : FragmentStateAdapter {
    private val fragments: ArrayList<Fragment> = arrayListOf()

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)
    constructor(fragment: FragmentActivity, fragments: List<Fragment>) : super(fragment) {
        this.fragments.clear()
        this.fragments.addAll(fragments)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}