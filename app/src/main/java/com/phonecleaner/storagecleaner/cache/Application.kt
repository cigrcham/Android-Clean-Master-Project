package com.phonecleaner.storagecleaner.cache

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        initMKKV()
    }

    private fun initMKKV() {
        MMKV.initialize(this)
        dataStore = MMKV.defaultMMKV()
    }

    companion object {
        lateinit var appContext: Context
        lateinit var dataStore: MMKV
    }
}