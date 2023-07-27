package com.phonecleaner.storagecleaner.cache.utils.singleton

class SingletonMenu private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonMenu

        fun getInstance(): SingletonMenu {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonMenu()
                }
                return instance
            }
        }
    }

    var type: Int = -1
    var function: String = ""
}