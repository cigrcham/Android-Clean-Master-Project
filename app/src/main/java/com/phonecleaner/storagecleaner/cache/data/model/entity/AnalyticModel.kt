package com.phonecleaner.storagecleaner.cache.data.model.entity

data class AnalyticModel(
    var title: Int = -1, var listFileApps: List<FileApp> = emptyList(), var size: Int = 0
)