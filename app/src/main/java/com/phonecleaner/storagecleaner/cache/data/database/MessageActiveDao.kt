package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MessageActiveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(fileDelete: MessageNotifi)

    @Query("SELECT * FROM `MessageNotifi`")
    abstract fun getAll(): Flow<List<MessageNotifi>>

    @Query("DELETE FROM `MessageNotifi` WHERE id = :id")
    abstract fun delete(id: Long)

    @Query("DELETE FROM `MessageNotifi` WHERE packageName = :packageName AND content = :content")
    abstract fun deleteByPackageAndContent(packageName: String, content: String)

    @Query("DELETE FROM `MessageNotifi`")
    abstract fun clearAll()

    @Query("SELECT * FROM `MessageNotifi` WHERE id = :id LIMIT 1")
    abstract fun getItem(id: Long): MessageNotifi?

    @Update
    abstract fun update(fileHide: MessageNotifi)
}