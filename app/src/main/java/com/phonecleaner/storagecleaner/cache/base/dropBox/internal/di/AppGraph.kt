package com.phonecleaner.storagecleaner.cache.base.dropBox.internal.di

import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxApiWrapper
import com.phonecleaner.storagecleaner.base.dropBox.internal.api.DropboxCredentialUtil
import com.phonecleaner.storagecleaner.cache.base.dropBox.internal.api.DropboxOAuthUtil

interface AppGraph {
    val dropboxCredentialUtil: DropboxCredentialUtil
    val dropboxOAuthUtil: DropboxOAuthUtil
    val dropboxApiWrapper: DropboxApiWrapper
}