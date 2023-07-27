package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentRecentBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentFragment : Fragment() {
    private lateinit var binding: FragmentRecentBinding
    private lateinit var adapterTab: RecentFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecentBinding.inflate(layoutInflater, container, false)
        adapterTab = RecentFragmentAdapter(fragment = this)
        binding.viewPagerRecent.adapter = adapterTab
        TabLayoutMediator(binding.tabLayoutRecent, binding.viewPagerRecent) { tab, position ->
            tab.text = if (position == 0) resources.getString(R.string.recent_tab_layout)
            else resources.getString(R.string.favorite_tab_layout)
        }.attach()
        return binding.root
    }

    inner class RecentFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            if (position == 0) RecentTabFragment() else FavoriteTabFragment()
    }
}