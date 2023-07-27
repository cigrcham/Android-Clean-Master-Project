package com.phonecleaner.storagecleaner.cache.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.phonecleaner.storagecleaner.cache.R
import java.util.concurrent.TimeUnit

fun formatTime(millis: Long): String {
    return when {
        millis > 3600000 -> String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
        else -> {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            )
        }
    }
}

fun ImageView.playOrPause(isShow: Boolean) {
    if (isShow) {
        this.setImageResource(R.drawable.ic_play_button)
    } else {
        this.setImageResource(R.drawable.ic_pause_button)
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun FragmentActivity.showDialog(dialog: DialogFragment, fragmentManager: FragmentManager) {
    if (!dialog.isAdded && !dialog.isVisible && !fragmentManager.isDestroyed) {
        dialog.show(fragmentManager, dialog.tag)
    }
}

@BindingAdapter("showView")
fun View.showOrHide(isShow: Boolean) {
    visibility = if (isShow) {
        View.VISIBLE
    } else {
        View.GONE
    }
}