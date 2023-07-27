package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep

@Keep
sealed class Screen {
    object Main : Screen()
    object Internal : Screen()
    object Image : Screen()
    object Audio : Screen()
    object Video : Screen()
    object App : Screen()
    object Document : Screen()
    object Zip : Screen()
    object Download : Screen()
    object Recently : Screen()
    object Dropbox : Screen()
    object Favorite : Screen()
    object Analytics : Screen()
    object RecycleBin : Screen()
    object ListFileAnalytics : Screen()
    object Search : Screen()
    object ListFileAlbum : Screen()
    object ListRecently : Screen()
}