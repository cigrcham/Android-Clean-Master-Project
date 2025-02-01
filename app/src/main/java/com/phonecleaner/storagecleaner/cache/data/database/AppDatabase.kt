package com.phonecleaner.storagecleaner.cache.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHistory
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import com.phonecleaner.storagecleaner.cache.data.model.response.DataTypeConverter

@Database(
    entities = [
        FileDelete::class,
        Account::class,
        FileApp::class,
        Folder::class,
        FileHistory::class,
        FileHide::class,
        AppInstalled::class,
        MessageNotifi::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DataTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAccountDao(): AccountDao
    abstract fun getFileDeleteDao(): FileDeleteDao
    abstract fun getFileFavoriteDao(): FileFavoriteDao
    abstract fun getFileHistoryDao(): FileHistoryDao
    abstract fun getFolderFavoriteDao(): FolderFavoriteDao
    abstract fun getFileHide(): FileHideDao
    abstract fun getAppBlock(): AppBlockDao
    abstract fun getMessageNotification(): MessageActiveDao

    companion object {
        private const val DATABASE_NAME = "AESTHETIC_SEARCH"
        private var INSTANCE: AppDatabase? = null

//        fun getInstance(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance: AppDatabase = Room
//                    .databaseBuilder(
//                        context.applicationContext,
//                        AppDatabase::class.java,
//                        DATABASE_NAME
//                    )
//                    .addMigrations(migration2To3, migration3To4, migration4To5)
//                    .fallbackToDestructiveMigration()
//                    .build()
//
//                INSTANCE = instance
//                instance
//            }
//        }

        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS FileHide(" + "id INTEGER PRIMARY KEY NOT NULL, " + "name TEXT NOT NULL, originPath TEXT NOT NULL, " + "currentPath TEXT NOT NULL, type TEXT NOT NULL, " + "size INTEGER NOT NULL, dateModified INTEGER NOT NULL)"
                )
            }
        }

        private val migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS AppInstalled(" + "id INTEGER PRIMARY KEY NOT NULL, " + "packageName TEXT NOT NULL, appName TEXT NOT NULL, " + "modified INTEGER NOT NULL, size INTEGER NOT NULL)"
                )
            }
        }

        private val migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS MessageNotifi(" + "id INTEGER PRIMARY KEY NOT NULL, " + "packageName TEXT NOT NULL, appName TEXT NOT NULL, " + "content TEXT NOT NULL, keyNotification TEXT NOT NULL, " + "title TEXT NOT NULL, idMessage INTEGER NOT NULL, modified INTEGER NOT NULL)"
                )
            }
        }
    }
}