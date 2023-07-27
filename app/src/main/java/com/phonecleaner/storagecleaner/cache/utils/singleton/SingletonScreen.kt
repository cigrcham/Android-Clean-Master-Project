package com.phonecleaner.storagecleaner.cache.utils.singleton

import com.phonecleaner.storagecleaner.cache.data.model.response.Screen

class SingletonScreen private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonScreen

        fun getInstance(): SingletonScreen {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonScreen()
                }
                return instance
            }
        }
    }

    var type: Screen = Screen.Main
}