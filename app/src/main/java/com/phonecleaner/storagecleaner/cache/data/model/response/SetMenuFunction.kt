package com.phonecleaner.storagecleaner.cache.data.model.response

import com.phonecleaner.storagecleaner.cache.utils.Constants

enum class SetMenuFunction(feature: String) {
    COPY(Constants.COPY),
    MOVE(Constants.MOVE),
    DELETE(Constants.DELETE),
    RENAME(Constants.RENAME),
//    CONVERT(Constants.CONVERT),
    HIDE(Constants.HIDE),
    UN_HIDE(Constants.UN_HIDE),
//    ZIP(Constants.ZIP),
    PROPERTIES(Constants.PROPERTIES),
    OPEN(Constants.OPEN),
    SHARE(Constants.SHARE),
    FAVORITE(Constants.FAVORITE),
    PASTE(Constants.PASTE),
    CREATE(Constants.CREATE),
    CANCEL(Constants.CANCEL),
    DELETE_RECYCLE_BIN(Constants.DELETE_RECYCLE_BIN),
    RESTOCK(Constants.RESTOCK),
    UNINSTALL(Constants.UNINSTALL),
    APK_SHARE(Constants.APK_SHARE),
    ACCOUNT_RENAME(Constants.ACCOUNT_RENAME),
    REMOVE(Constants.REMOVE),
    EXTRACT(Constants.EXTRACT),
    MOVE_TO_INTERNAL(Constants.MOVE_TO_INTERNAL),
    MOVE_TO_DROPBOX(Constants.MOVE_TO_DROPBOX),
    DOWNLOAD(Constants.DOWNLOAD)
}