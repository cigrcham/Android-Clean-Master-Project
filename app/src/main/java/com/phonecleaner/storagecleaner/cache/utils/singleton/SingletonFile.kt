package com.phonecleaner.storagecleaner.cache.utils.singleton

import java.io.File

class SingletonFile private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonFile

        fun getInstance(): SingletonFile {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonFile()
                }
                return instance
            }
        }
    }

    var oldFile: File? = null
    var newFile: File? = null
}