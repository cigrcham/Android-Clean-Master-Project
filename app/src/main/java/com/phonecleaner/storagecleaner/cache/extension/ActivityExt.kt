package com.phonecleaner.storagecleaner.cache.extension


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.phonecleaner.storagecleaner.cache.BuildConfig
import com.phonecleaner.storagecleaner.cache.ui.dialog.PermissionDialog
import java.io.File

fun Context.managerStoragePermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager()
    else checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun Context.managerPostNotificationPermissionGranted(): Boolean {
    return this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

fun AppCompatActivity.requestPermissionManagerStorage(
    launcher: ActivityResultLauncher<Intent>,
    launcherLess30: ActivityResultLauncher<String>,
) {
    try {
        if (Build.VERSION.SDK_INT >= 30) {
            val dialogPermission = PermissionDialog()
            dialogPermission.callBack = { isAccept ->
                if (isAccept) {
                    launcher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        )
                    )
                }
            }
            dialogPermission.show(supportFragmentManager, "")
        } else {
            launcherLess30.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    } catch (ex: Exception) {
//        toast(this.getString(R.string.error_preview_image))
    }
}

fun Activity.handleOpenDocumentFile(path: String) {
    val fileUri = FileProvider.getUriForFile(
        this, this.applicationContext.packageName + ".provider", File(path)
    )
    val intent = Intent(Intent.ACTION_VIEW)
    if (fileUri.toString().contains(".doc") || fileUri.toString()
            .contains(".xlsx") || fileUri.toString().contains(".docx") || fileUri.toString()
            .contains(".pptx")
    ) {
        intent.setDataAndType(fileUri, "application/msword")
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}

fun Activity.resetActivity() {
    val intent = this.intent
    this.overridePendingTransition(0, 0)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    this.finish()
    this.overridePendingTransition(0, 0)
    this.startActivity(intent)
}