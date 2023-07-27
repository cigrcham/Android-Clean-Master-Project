package com.phonecleaner.storagecleaner.cache.utils.singleton

import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp

class SingletonListFileApp private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonListFileApp

        fun getInstance(): SingletonListFileApp {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonListFileApp()
                }
                return instance
            }
        }
    }

    var listFileApp = mutableListOf<FileApp>()
}