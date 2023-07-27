package com.phonecleaner.storagecleaner.cache.extension

import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder


fun MutableList<FileApp>.setSelectedFile(isSelect: Boolean) {
    this.forEach { fileApp ->
        fileApp.isSelected = isSelect
    }
}

fun MutableList<Folder>.setSelectedFolder(isSelect: Boolean) {
    this.forEach { fileApp ->
        fileApp.selected = isSelect
    }
}

fun MutableList<Account>.setSelectedAccount(isSelect: Boolean) {
    this.forEach { account ->
        account.isSelected = isSelect
    }
}

fun MutableList<AppInstalled>.setSelectedApp(isSelect: Boolean) {
    this.forEach { appInstalled ->
        appInstalled.selected = isSelect
    }
}

//fun MutableList<FileDelete>.setSelectedFileDelete(isSelect: Boolean) {
//    this.forEach { fileDelete ->
//        fileDelete.isSelected = isSelect
//    }
//}
//
//fun MutableList<FileHide>.setSelectedFileHide(isSelect: Boolean) {
//    this.forEach { fileDelete ->
//        fileDelete.isSelected = isSelect
//    }
//}
