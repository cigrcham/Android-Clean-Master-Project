package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(app: AppInstalled)

    @Query("SELECT * FROM `AppInstalled`")
    abstract fun getAll(): Flow<List<AppInstalled>>

    @Query("DELETE FROM `AppInstalled` WHERE packageName = :packageName")
    abstract fun delete(packageName: String)

    @Query("DELETE FROM `AppInstalled`")
    abstract fun clearAll()

    @Query("SELECT * FROM `AppInstalled` WHERE id = :id LIMIT 1")
    abstract fun getItem(id: Long): AppInstalled?

    @Update
    abstract fun update(fileHide: AppInstalled)

    @Delete
    abstract fun deleteItem(apps: AppInstalled)
}