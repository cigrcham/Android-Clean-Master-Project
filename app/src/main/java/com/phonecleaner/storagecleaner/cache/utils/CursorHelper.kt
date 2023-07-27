package com.phonecleaner.storagecleaner.cache.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

object CursorHelper {

    fun getIdFromDisplayName(context: Context, displayName: String): Long? {
        val projection: Array<String> = arrayOf(MediaStore.Files.FileColumns._ID)
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), projection,
            MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", arrayOf(displayName), null
        )
        cursor?.let { cur ->
            cur.moveToFirst()
            if (cur.count > 0) {
                val columnIndex = cur.getColumnIndex(projection[0])
                val fileId = cur.getLong(columnIndex)
                cur.close()
                return fileId
            }
        }
        return null
    }
}