package com.phonecleaner.storagecleaner.cache.data.model.entity

data class TypeConvert(var type: ConvertType, var isSelected: Boolean = false) {
    var name: String = when (type) {
        ConvertType.PDF -> "PDF"
        ConvertType.IMAGE_PDF -> "PDF"
        ConvertType.MP4_WAV -> "WAV"
        ConvertType.MP4_M4A -> "M4A"
        ConvertType.MP4_AVI -> "AVI"
        ConvertType.MP4_M0V -> "MOV"
        ConvertType.MP4_MP3 -> "MP3"
        ConvertType.MP3_WAV -> "MP3_WAV"
        ConvertType.MP3_M4A -> "MP3_M4A"
        ConvertType.MP3_AIFF -> "MP3_AIFF"
        ConvertType.XLSX -> "XLSX"
        else -> ""
    }
}