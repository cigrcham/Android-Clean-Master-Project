package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.View
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentMenuFileBinding
import com.phonecleaner.storagecleaner.cache.base.BaseViewStubFragment

class MenuFileFragment : BaseViewStubFragment<FragmentMenuFileBinding>() {

    override fun onCreateViewAfterViewStubInflated(
        binding: FragmentMenuFileBinding, inflatedView: View, savedInstanceState: Bundle?
    ) {
    }

    override fun getViewStubLayoutResource(): Int {
        return R.layout.fragment_menu_file
    }
}