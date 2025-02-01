package com.phonecleaner.storagecleaner.cache.services

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import com.phonecleaner.storagecleaner.cache.data.database.AppDatabase
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageNotifi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BlockNotification : NotificationListenerService() {
    var database: AppDatabase? = null
    override fun onCreate() {
        super.onCreate()
//        database = AppDatabase.getInstance(this@BlockNotification)
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onListenerConnected() {
        super.onListenerConnected()
        GlobalScope.launch(Dispatchers.IO) {
            database?.getMessageNotification()?.clearAll()
            activeNotifications.forEach {
                val packageName: String = it.packageName
                val pm: PackageManager = packageManager
                var appName = "Application"
                try {
                    appName = pm.getApplicationLabel(
                        pm.getApplicationInfo(
                            packageName, PackageManager.GET_META_DATA
                        )
                    ).toString()
                } catch (ex: PackageManager.NameNotFoundException) {
                    ex.printStackTrace()
                }
                val title: String =
                    it.notification?.extras?.get(Notification.EXTRA_TITLE).toString()
                val content: String =
                    it.notification?.extras?.get(Notification.EXTRA_TEXT).toString()
                if ((title != "null" || content != "null")) {
                    val message = MessageNotifi(
                        packageName = it.packageName,
                        content = content,
                        title = title,
                        appName = appName,
                        modified = it.postTime,
                        keyNotification = it.key,
                        idMessage = it.id
                    )
                    database?.getMessageNotification()?.insert(message)
                }
            }
        }
    }
}