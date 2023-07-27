package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp

@Dao
abstract class FileFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(fileApp: FileApp)

    @Query("SELECT * FROM file where favorite = 1")
    abstract fun getAll(): List<FileApp>

    @Query("DELETE FROM file WHERE id = :id and favorite = 1")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM file WHERE id = :id and favorite = 1 LIMIT 1")
    abstract fun getItem(id: Long): FileApp?

    @Query("SELECT EXISTS(SELECT * FROM file WHERE id = :id LIMIT 1)")
    abstract fun getItemFavorite(id: Long): Boolean

    @Update
    abstract fun update(fileApp: FileApp)
}