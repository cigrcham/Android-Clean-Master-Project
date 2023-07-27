package com.phonecleaner.storagecleaner.cache.utils.singleton

class SingletonPath private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonPath

        fun getInstance(): SingletonPath {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonPath()
                }
                return instance
            }
        }
    }

    var path: String = ""
}