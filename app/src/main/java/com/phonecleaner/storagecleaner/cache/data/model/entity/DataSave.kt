package com.phonecleaner.storagecleaner.cache.data.model.entity

import com.phonecleaner.storagecleaner.cache.Application
import com.phonecleaner.storagecleaner.cache.utils.Constants.LANGUAGE_NOW

object DataSave {
    var language: String?
        get() {
            return Application.dataStore.getString(LANGUAGE_NOW, "en")
        }
        set(value) {
            Application.dataStore.putString(LANGUAGE_NOW, value)
        }
}