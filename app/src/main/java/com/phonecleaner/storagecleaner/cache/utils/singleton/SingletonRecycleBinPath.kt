package com.phonecleaner.storagecleaner.cache.utils.singleton

class SingletonRecycleBinPath private constructor() {
    companion object {
        @Volatile
        private lateinit var instance: SingletonRecycleBinPath

        fun getInstance(): SingletonRecycleBinPath {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonRecycleBinPath()
                }
                return instance
            }
        }
    }

    var path: String = ""
}