package com.phonecleaner.storagecleaner.cache.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phonecleaner.storagecleaner.cache.data.database.AppDatabase
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHistory
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import com.phonecleaner.storagecleaner.cache.extension.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {
    suspend fun setFirstInstall(isCheck: Boolean) {
        context.dataStore.edit {
            it[IS_FIRST_TIME_INSTALLING] = isCheck
        }
    }

    val isFirstInstall: Flow<Boolean> = context.dataStore.data.catch { exception ->
        exception.message?.let { Timber.tag("Repository").e(it) }
        emit(emptyPreferences())
    }.map { preference ->
        preference[IS_FIRST_TIME_INSTALLING] ?: true
    }

    suspend fun setPasscode(passcode: String) {
        context.dataStore.edit {
            it[PASSCODE] = passcode
        }
    }

    suspend fun setPasscodeHidden(passcode: String) {
        context.dataStore.edit {
            it[PASSCODE_HIDDEN] = passcode
        }
    }

    suspend fun setShowRecently(isShow: Boolean) {
        context.dataStore.edit {
            it[IS_SHOW_RECENTLY] = isShow
        }
    }

    val isShowRecently: Flow<Boolean> = context.dataStore.data.catch { exception ->
        exception.message?.let { Timber.tag("Repository").e(it) }
        emit(emptyPreferences())
    }.map { preference ->
        preference[IS_SHOW_RECENTLY] ?: true
    }

    val getPasscode: Flow<String> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
        } else {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
            throw exception
        }
    }.map { preference ->
        preference[PASSCODE] ?: ""
    }

    val getPasscodeHidden: Flow<String> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
        } else {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
            throw exception
        }
    }.map { preference ->
        preference[PASSCODE_HIDDEN] ?: ""
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit {
            it[LANGUAGE] = lang
        }
    }

    val getLang: Flow<String> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
        } else {
            exception.message?.let { Timber.tag("Repository").e(it) }
            emit(emptyPreferences())
            throw exception
        }
    }.map { preference ->
        preference[LANGUAGE] ?: "en"
    }

    val dropboxMail: Flow<String> = context.dataStore.data.catch { exception ->
        exception.message?.let { Timber.tag("Repository").e(it) }
        emit(emptyPreferences())
    }.map { preference ->
        preference[DROPBOX_MAIL] ?: ""
    }

    suspend fun setDropboxMail(mail: String) {
        context.dataStore.edit {
            it[DROPBOX_MAIL] = mail
        }
    }

    fun insertFileHistory(fileHistory: FileHistory) =
        database.getFileHistoryDao().insert(fileHistory)

    fun getAllFileHistory(): Flow<List<FileHistory>> = database.getFileHistoryDao().getAll()

    fun deleteFileHistory(id: Long) = database.getFileHistoryDao().delete(id)

    fun getFileHistory(id: Long) = database.getFileHistoryDao().getItem(id)

    fun clearHistory() = database.getFileHistoryDao().clearAll()


    fun insertFileDelete(fileDelete: FileDelete) = database.getFileDeleteDao().insert(fileDelete)
    fun insertFileHide(fileDelete: FileHide) = database.getFileHide().insert(fileDelete)
    fun insertAppBlock(app: AppInstalled) = database.getAppBlock().insert(app)

    fun getAllFileDelete(): Flow<List<FileDelete>> {
        return database.getFileDeleteDao().getAll()
    }

    fun getAllFileHide(): Flow<List<FileHide>> {
        return database.getFileHide().getAll()
    }

    fun getAllMessage(): Flow<List<MessageNotifi>> {
        return database.getMessageNotification().getAll()
    }

    fun getAllAppBlock(): Flow<List<AppInstalled>> {
        return database.getAppBlock().getAll()
    }

    fun deleteFileDelete(id: Long) = database.getFileDeleteDao().delete(id)
    fun deleteFileHide(id: Long) = database.getFileHide().delete(id)
    fun deleteAppBlock(item: AppInstalled) = database.getAppBlock().delete(item.packageName)
    fun updateFileHide(fileHide: FileHide) = database.getFileHide().update(fileHide)

    fun clearAll() = database.getFileDeleteDao().clearAll()
    fun clearAllAppSelected() = database.getAppBlock().clearAll()

    fun getFileDelete(id: Long) = database.getFileDeleteDao().getItem(id)

    fun insertFolderFavorite(folder: Folder) = database.getFolderFavoriteDao().insert(folder)

    fun getAllFolderFavorite(): List<Folder> {
        return database.getFolderFavoriteDao().getAll()
    }

    fun deleteFolderFavorite(id: Long) = database.getFolderFavoriteDao().delete(id)

    fun getFolderFavorite(id: Long) = database.getFolderFavoriteDao().getItem(id)

    fun insertFileFavorite(fileApp: FileApp) = database.getFileFavoriteDao().insert(fileApp)

    fun getAllFileFavorite(): List<FileApp> {
        return database.getFileFavoriteDao().getAll()
    }

    fun deleteFileFavorite(id: Long) = database.getFileFavoriteDao().delete(id)

    fun getFileFavorite(id: Long) = database.getFileFavoriteDao().getItem(id)
    fun getItemFavorite(id: Long): Boolean = database.getFileFavoriteDao().getItemFavorite(id)

    fun updateFileFavorite(fileApp: FileApp) = database.getFileFavoriteDao().update(fileApp)

    fun insertAccount(account: Account) = database.getAccountDao().insert(account)

    fun getAllAccounts(type: String) = database.getAccountDao().getAll(type)

    fun getAccount(email: String) = database.getAccountDao().getItem(email)

    fun deleteAccount(id: Long) = database.getAccountDao().delete(id)

    fun renameAccount(account: Account) = database.getAccountDao().renameAccount(account)

    companion object {
        val IS_FIRST_TIME_INSTALLING = booleanPreferencesKey("IS_FIRST_TIME_INSTALLING")
        val PASSCODE = stringPreferencesKey("PASSCODE")
        val PASSCODE_HIDDEN = stringPreferencesKey("PASSCODE_HIDDEN")
        val LANGUAGE = stringPreferencesKey("LANGUAGE")
        val IS_SHOW_RECENTLY = booleanPreferencesKey("IS_SHOW_RECENTLY")
        val DROPBOX_MAIL = stringPreferencesKey("DROPBOX_MAIL")
    }
}