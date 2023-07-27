package com.phonecleaner.storagecleaner.cache.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.phonecleaner.storagecleaner.cache.BuildConfig
import com.phonecleaner.storagecleaner.cache.R

object AppActionHelper {
    fun openRate(context: Context) {
        startActivity(
            context,
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
        )
    }

    fun share(context: Context) {
        val appPackageName = BuildConfig.APPLICATION_ID
        val appName = context.getString(R.string.app_name)
        val appDesc = context.getString(R.string.app_decs)
        val shareBodyText =
            "$appDesc \n https://play.google.com/store/apps/details?id=$appPackageName"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, appName)
            putExtra(Intent.EXTRA_TEXT, shareBodyText)
        }
        startActivity(context, Intent.createChooser(sendIntent, null))
    }

    fun openMail(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL))
        startActivity(context, intent)
    }

    private fun startActivity(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.common_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private const val EMAIL = "trustedapp.support@apero.vn"
    private const val URL_POLICY = "https://mobile-smart-growth.web.app"
    private const val URL_TERM = "https://mobile-smart-growth.web.app"

}
