package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FileDeleteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(fileDelete: FileDelete)

    @Query("SELECT * FROM `file delete`")
    abstract fun getAll(): Flow<List<FileDelete>>

    @Query("DELETE FROM `file delete` WHERE id = :id")
    abstract fun delete(id: Long)

    @Query("DELETE FROM `file delete`")
    abstract fun clearAll()

    @Query("SELECT * FROM `file delete` WHERE id = :id LIMIT 1")
    abstract fun getItem(id: Long): FileDelete?
}