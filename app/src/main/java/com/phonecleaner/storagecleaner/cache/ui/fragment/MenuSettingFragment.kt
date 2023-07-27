package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.View
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentMenuSettingBinding
import com.phonecleaner.storagecleaner.cache.base.BaseViewStubFragment

class MenuSettingFragment : BaseViewStubFragment<FragmentMenuSettingBinding>() {
    override fun onCreateViewAfterViewStubInflated(
        binding: FragmentMenuSettingBinding, inflatedView: View, savedInstanceState: Bundle?
    ) {
    }

    override fun getViewStubLayoutResource(): Int {
        return R.layout.fragment_menu_setting
    }
}