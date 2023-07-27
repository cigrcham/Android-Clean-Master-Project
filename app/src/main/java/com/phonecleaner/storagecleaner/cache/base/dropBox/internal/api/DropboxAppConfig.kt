package com.phonecleaner.storagecleaner.cache.base.dropBox.internal.api


import com.phonecleaner.storagecleaner.cache.BuildConfig

class DropboxAppConfig(
    val apiKey : String = BuildConfig.DROPBOX_APP_KEY,
    val clientIdentifier: String = "db-${apiKey}"
)