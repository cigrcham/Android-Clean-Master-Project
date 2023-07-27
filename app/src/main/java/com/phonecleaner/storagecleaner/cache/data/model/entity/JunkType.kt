package com.phonecleaner.storagecleaner.cache.data.model.entity

class JunkType(
    var typeName: String,
    var icon: Int,
    var size: Long,
    var slected: Boolean = true,
    var isCanShowMore: Boolean = true,
    var listFile: ArrayList<FileApp>
){
    var isShowMore: Boolean = false
}