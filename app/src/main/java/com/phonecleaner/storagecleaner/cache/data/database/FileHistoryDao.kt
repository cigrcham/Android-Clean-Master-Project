package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHistory
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FileHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(fileHistory: FileHistory)

    @Query("SELECT * FROM `file history`")
    abstract fun getAll(): Flow<List<FileHistory>>

    @Query("DELETE FROM `file history` WHERE id = :id")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM `file history` WHERE id = :id LIMIT 1")
    abstract fun getItem(id: Long): FileHistory?

    @Query("DELETE FROM `file history`")
    abstract fun clearAll()
}