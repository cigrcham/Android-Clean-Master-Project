package com.phonecleaner.storagecleaner.cache.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseCacheFragment<T : ViewBinding> : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (!this::binding.isInitialized) {
            binding = createView(inflater, container)
        }
        return binding.root
    }

    abstract fun createView(
        inflater: LayoutInflater, container: ViewGroup?
    ): T
}