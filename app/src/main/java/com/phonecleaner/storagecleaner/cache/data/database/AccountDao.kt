package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.*
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account

@Dao
abstract class AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(account: Account)

    @Query("SELECT * FROM account WHERE type = :type")
    abstract fun getAll(type: String): List<Account>

    @Query("DELETE FROM account WHERE id = :id")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM account WHERE email = :email LIMIT 1")
    abstract fun getItem(email: String): Account?

    @Update
    abstract fun renameAccount(newName: Account)
}