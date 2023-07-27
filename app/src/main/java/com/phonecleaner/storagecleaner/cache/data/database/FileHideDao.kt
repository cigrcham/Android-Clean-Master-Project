package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import kotlinx.coroutines.flow.Flow
@Dao
abstract class FileHideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(fileDelete: FileHide)

    @Query("SELECT * FROM `FileHide`")
    abstract fun getAll(): Flow<List<FileHide>>

    @Query("DELETE FROM `FileHide` WHERE id = :id")
    abstract fun delete(id: Long)

    @Query("DELETE FROM `FileHide`")
    abstract fun clearAll()

    @Query("SELECT * FROM `FileHide` WHERE id = :id LIMIT 1")
    abstract fun getItem(id: Long): FileHide?

    @Update
    abstract fun update(fileHide: FileHide)
}