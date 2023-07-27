package com.phonecleaner.storagecleaner.cache.utils.singleton

class SingletonDropboxMail private constructor() {

    companion object {
        @Volatile
        private lateinit var instance: SingletonDropboxMail

        fun getInstance(): SingletonDropboxMail {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = SingletonDropboxMail()
                }
                return instance
            }
        }
    }

    var mail: String = ""
}