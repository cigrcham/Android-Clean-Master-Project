package com.phonecleaner.storagecleaner.cache.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.datastore.preferences.preferencesDataStore
import com.phonecleaner.storagecleaner.cache.BuildConfig
import com.phonecleaner.storagecleaner.cache.R
import timber.log.Timber
import java.io.File

private const val myTag = "ContextExt"

val Context.dataStore by preferencesDataStore(name = "DATA_STORE")

fun Context.toast(notify: String) = Toast.makeText(this, notify, Toast.LENGTH_SHORT).show()

fun Context.getBannerWidth(): Int {
    return resources.displayMetrics.widthPixels / 10 * 7
}

fun Context.dp2px(dp: Float): Int {
    return (dp * resources.displayMetrics.density + 0.5f).toInt()
}

@SuppressLint("ServiceCast")
fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Timber.tag("Internet").e("NetworkCapabilities.TRANSPORT_WIFI")
            true
        } else {
            Timber.tag("Internet").e("TRANSPORT_WIFI Fails!!!")
            false
        }
    }
    return false
}

fun Context.shareMultiFile(pathList: MutableList<String>, type: String) {
    try {
        val intent = Intent()
        intent.type = "$type/*"
        val uris: ArrayList<Uri> = ArrayList()
        for (path in pathList) {
            val uri: Uri = FileProvider.getUriForFile(
                this, BuildConfig.APPLICATION_ID + ".provider", File(path)
            )
            uris.add(uri)
        }
        if (uris.size == 1) {
            intent.setDataAndType(uris[0], contentResolver.getType(uris[0]))
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, uris[0])
        } else {
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, this.getString(R.string.app_name)))
    } catch (e: Exception) {
        Timber.tag(myTag).e("share failed: ${e.message}")
    }
}

fun Context.shareFile(file: File) {
    try {
        val intent = Intent()
        intent.type = file.getMimeType()
        val uri: Uri =
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        intent.setDataAndType(uri, contentResolver.getType(uri))
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, this.getString(R.string.app_name)))
    } catch (e: Exception) {
        Timber.tag(myTag).e("Share failed: ${e.message}")
    }
}

fun Context.shareApplication(filePath: String, appName: String) {
    val shareBodyText = "https://play.google.com/store/apps/details?id=$filePath"
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, appName)
        putExtra(Intent.EXTRA_TEXT, shareBodyText)
    }
    startActivity(Intent.createChooser(sendIntent, null))
}

@SuppressLint("MissingPermission")
fun Context.isOnMobileData(): Boolean {
    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        tm.isDataEnabled
    } else {
        tm.simState == TelephonyManager.SIM_STATE_READY && tm.dataState != TelephonyManager.DATA_DISCONNECTED
    }
}
