package com.phonecleaner.storagecleaner.cache.data.model.entity

import com.phonecleaner.storagecleaner.cache.utils.TypeAction

data class MessageEvent(val type: TypeAction? = null, val list: ArrayList<FileApp>? = null)
