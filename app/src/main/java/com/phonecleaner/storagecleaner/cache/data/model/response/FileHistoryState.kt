//package com.phonecleaner.storagecleaner.cache.data.model.response
//
//import androidx.annotation.Keep
//import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHistory
//
//@Keep
//sealed class FileHistoryState {
//    data class Success(val listFile: List<FileHistory>) : FileHistoryState()
//    data class Error(val exception: Throwable) : FileHistoryState()
//}