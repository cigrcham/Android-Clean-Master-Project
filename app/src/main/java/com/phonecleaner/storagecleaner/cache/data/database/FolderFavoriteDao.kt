package com.phonecleaner.storagecleaner.cache.data.database

import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import androidx.room.*

@Dao
abstract class FolderFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(folder: Folder)

    @Query("SELECT * FROM folder where favorite = 1")
    abstract fun getAll(): List<Folder>

    @Query("DELETE FROM folder WHERE id = :id and favorite = 1")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM folder WHERE id = :id and favorite = 1 LIMIT 1")
    abstract fun getItem(id: Long): Folder?
}