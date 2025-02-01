package com.phonecleaner.storagecleaner.cache.base.dropBox.internal.di;

import android.content.Context
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxApiWrapper
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxCredentialUtil
import com.phonecleaner.storagecleaner.cache.base.dropBox.internal.api.DropboxAppConfig
import com.phonecleaner.storagecleaner.cache.base.dropBox.internal.api.DropboxOAuthUtil

internal class AppGraphImpl(context: Context) : AppGraph {
    private val dropboxAppConfig = DropboxAppConfig()

    override val dropboxCredentialUtil by lazy { DropboxCredentialUtil(context.applicationContext) }

    override val dropboxOAuthUtil by lazy {
        DropboxOAuthUtil(
            dropboxAppConfig = dropboxAppConfig,
            dropboxCredentialUtil = dropboxCredentialUtil
        )
    }

    override val dropboxApiWrapper
        get() = DropboxApiWrapper(
            dbxCredential = dropboxCredentialUtil.readCredentialLocally()!!,
            clientIdentifier = dropboxAppConfig.clientIdentifier
        )
}
