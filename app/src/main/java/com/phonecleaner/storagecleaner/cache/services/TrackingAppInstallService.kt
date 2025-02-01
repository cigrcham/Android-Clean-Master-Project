package com.phonecleaner.storagecleaner.cache.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity

class TrackingAppInstallService : Service() {
    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
//        createNotificationChannel(CHANNEL_ID)
//        val notification = createNotification()
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(applicationContext)
//                .notify(NOTIFICATION_SERVICE_ID, notification)
//            startForeground(NOTIFICATION_SERVICE_ID, notification)
//            registerAppInstallReceiver()
//        }
    }

    private fun createNotification(): Notification {
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_MAX).build()
        notification.flags = Notification.FLAG_AUTO_CANCEL
        return notification
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun registerAppInstallReceiver() {
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        screenFilter.addAction(Intent.ACTION_PACKAGE_INSTALL)
        screenFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        screenFilter.addDataScheme("package")
        registerReceiver(appInstallReceiver, screenFilter)
    }

    private var appInstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_PACKAGE_ADDED || intent?.action == Intent.ACTION_PACKAGE_INSTALL) {
                onInstallNewApp(intent.data.toString())
            }
        }
    }

    private fun onInstallNewApp(pkgName: String?) {
        pkgName?.let {
            if (it.contains("package:")) {
                val newPkgName = pkgName.removePrefix("package:")
                showNotiSuggestClean(newPkgName)
            } else {
                showNotiSuggestClean(pkgName)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotiSuggestClean(newPkgName: String) {
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCustomContentView(getNotificationLayoutSmall())
                .setCustomBigContentView(getNotificationLayoutBig()).setAutoCancel(true).build()
        notification.flags = Notification.FLAG_AUTO_CANCEL
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_CLEAN_ID, notification)
        }
    }

    private fun getNotificationLayoutSmall(): RemoteViews {
        val notificationLayout =
            RemoteViews(packageName, R.layout.layout_notification_phone_boost_small)
        notificationLayout.setTextViewText(
            R.id.tvTitle, getString(R.string.scan_junk_now)
        )
        notificationLayout.setOnClickPendingIntent(R.id.button, TaskStackBuilder.create(this).let {
            it.addNextIntentWithParentStack(Intent(
                this, MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = ACTION_BOOST_NOW
            })
            it.getPendingIntent(
                CODE_BOOST_NOW, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        })
        return notificationLayout
    }


    private fun getNotificationLayoutBig(): RemoteViews {
        val notificationLayout = RemoteViews(packageName, R.layout.layout_notification_phone_boost)
        notificationLayout.setTextViewText(
            R.id.tvDec, getString(R.string.scan_junk_now)
        )
        notificationLayout.setOnClickPendingIntent(R.id.imgClose,
            TaskStackBuilder.create(this).let {
                it.addNextIntentWithParentStack(Intent(
                    this, MainActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    action = ACTION_CLOSE_NOTI
                })
                it.getPendingIntent(
                    CODE_CLOSE_NOTIFICATION,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            })

        notificationLayout.setOnClickPendingIntent(R.id.button, TaskStackBuilder.create(this).let {
            it.addNextIntentWithParentStack(Intent(
                this, MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = ACTION_BOOST_NOW
            })
            it.getPendingIntent(
                CODE_BOOST_NOW, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        })
        return notificationLayout
    }

    companion object {
        const val NOTIFICATION_SERVICE_ID = 11
        const val NOTIFICATION_CLEAN_ID = 12
        const val CHANNEL_ID = "TRACKING_APP"
        const val CODE_CLOSE_NOTIFICATION = 1
        const val CODE_BOOST_NOW = 2
        const val ACTION_CLOSE_NOTI = "CLOSE_NOTI"
        const val ACTION_BOOST_NOW = "BOOST_NOW"
    }

}